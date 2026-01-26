package springboot.billgates.domain.billing.batch.job;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.domain.admin.repository.ReservationSettingRepository;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.model.BillingDiscountModel;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.domain.billing.batch.sql.BillingSqls;
import springboot.billgates.entity.ReservationSetting;

@Slf4j
@RequiredArgsConstructor
public class BillingCompositeWriter implements ItemWriter<BillingPack> {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReservationSettingRepository reservationSettingRepository;

    private boolean isGlobalReservation;
    private LocalDateTime cachedGlobalBaseTime;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        ReservationSetting setting =  reservationSettingRepository.findById(1L)
            .orElse(ReservationSetting.builder()
                .isReservationActive(false)
                .build());
        this.isGlobalReservation = setting.getIsReservationActive();
        if (this.isGlobalReservation) {
            this.cachedGlobalBaseTime = LocalDateTime.of(LocalDate.now(), setting.getReservationTime());
        }
    }

    @Override
    public void write(Chunk<? extends BillingPack> chunk) {
//        log.info(">>> [Writer] Saving Chunk... (Size: {} items)", chunk.getItems().size());
        long startTime = System.currentTimeMillis();

        List<Object[]> billingArgs = new ArrayList<>();
        List<Object[]> itemArgs = new ArrayList<>();
        List<Object[]> discountArgs = new ArrayList<>();
        List<Object[]> messageArgs = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (BillingPack pack : chunk) {
            long billingId = TSID.fast().toLong();

            // 1. Billing 적재
            billingArgs.add(new Object[] {
                billingId,
                pack.getBilling().getMemberId(),
                pack.getBilling().getBillingMonth(),
                pack.getBilling().getOriginalAmount(),
                pack.getBilling().getTotalDiscountAmount(),
                pack.getBilling().getTotalAmount(),
                Timestamp.valueOf(pack.getBilling().getCreatedAt())
            });

            // 2. Billing Item 적재
            if (!pack.getItems().isEmpty()) {
                for (BillingItemModel item : pack.getItems()) {
                    itemArgs.add(new Object[] {
                        billingId,
                        item.getCategory(),
                        item.getItemName(),
                        item.getAmount()
                    });
                }
            }

            // 3. Billing Discount 적재
            if (pack.getDiscounts() != null && !pack.getDiscounts().isEmpty()) {
                for (BillingDiscountModel discount : pack.getDiscounts()) {
                    long discountId = TSID.fast().toLong();
                    discountArgs.add(new Object[] {
                        discountId,
                        billingId,
                        discount.getPolicyId(),
                        discount.getDiscountName(),
                        discount.getDiscountAmount(),
                        discount.getTargetCategory()
                    });
                }
            }

            // 4. Message 적재
            LocalDateTime baseTime = this.isGlobalReservation ? this.cachedGlobalBaseTime : now;
            long messageId = TSID.fast().toLong();
            LocalDateTime reservedAt = calculateReservedTime(baseTime, pack);

            Map<String, Object> variables = new HashMap<>();
            variables.put("email", pack.getEmail());
            variables.put("phoneNumber", pack.getPhoneNumber());
            variables.put("month", pack.getBilling().getBillingMonth());

            // 3가지 금액
            variables.put("originalAmount", formatMoney(pack.getBilling().getOriginalAmount()));
            variables.put("discountAmount", "-" + formatMoney(pack.getBilling().getTotalDiscountAmount()));
            variables.put("totalAmount", formatMoney(pack.getBilling().getTotalAmount()));

            // 상세 목록 문자열 생성
            variables.put("itemList", generateItemListString(pack.getItems()));
            variables.put("discountList", generateDiscountListString(pack.getDiscounts()));

            String jsonBody = "{}";
            try {
                jsonBody = objectMapper.writeValueAsString(variables);
            } catch (Exception e){
                log.error("Failed to convert variables to JSON. memberId: {}", pack.getBilling().getMemberId(), e);
            }

            messageArgs.add(new Object[] {
                messageId,
                pack.getBilling().getMemberId(),
                billingId,
                "EMAIL",
                "READY",
                Timestamp.valueOf(reservedAt),
                Timestamp.valueOf(now),
                1L,
                pack.getBilling().getBillingMonth(),
                jsonBody,
                pack.getEmail(),
                pack.getPhoneNumber()
            });
        }

        // Bulk Insert 실행
        if (!billingArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING, billingArgs);
        if (!itemArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_ITEM, itemArgs);
        if (!discountArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_DISCOUNT, discountArgs); // [New]
        if (!messageArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_MESSAGE, messageArgs);

        long endTime = System.currentTimeMillis();
        log.info(">>> [Writer] Saved Complete. (Time taken: {}ms)", (endTime - startTime));
    }

    /**
     * [DnD 계산 로직]
     * 현재 시간이 사용자의 금지 시간대(Start ~ End)에 포함되면 -> End 시간으로 예약
     * 포함되지 않으면 -> 현재 시간(즉시 발송)
     */
    private LocalDateTime calculateReservedTime(LocalDateTime baseTime, BillingPack pack) {
        // DnD 미사용자거나 정보가 없으면 즉시 return
        if (!pack.isUseDnd() || pack.getDndStartTime() == null || pack.getDndEndTime() == null) {
            return baseTime;
        }

        LocalTime currentTime = baseTime.toLocalTime();
        Time sqlStartTime = pack.getDndStartTime(); // java.sql.Time
        Time sqlEndTime = pack.getDndEndTime();     // java.sql.Time

        LocalTime start = sqlStartTime.toLocalTime();
        LocalTime end = sqlEndTime.toLocalTime();

        boolean inDndTime = false;
        LocalDateTime targetTime = baseTime;

        // Case A: 자정을 걸치는 경우 (예: 22:00 ~ 07:00)
        if (start.isAfter(end)) {
            if (currentTime.isAfter(start) || currentTime.isBefore(end)) {
                inDndTime = true;

                // 현재가 22시, 23시라면 -> 내일 07시
                if (currentTime.isAfter(start)) {
                    targetTime = baseTime.plusDays(1).withHour(end.getHour()).withMinute(end.getMinute()).withSecond(0);
                }
                // 현재가 01시, 06시라면 -> 오늘 07시
                else {
                    targetTime = baseTime.withHour(end.getHour()).withMinute(end.getMinute()).withSecond(0);
                }
            }
        }
        // Case B: 당일 시간대 (예: 09:00 ~ 12:00) - 보통 드물지만 처리
        else {
            if (currentTime.isAfter(start) && currentTime.isBefore(end)) {
                inDndTime = true;
                // 끝나는 시간으로 설정
                targetTime = baseTime.withHour(end.getHour()).withMinute(end.getMinute()).withSecond(0);
            }
        }
        return inDndTime ? targetTime : baseTime;
    }

    private String formatMoney(long amount) {
        return new DecimalFormat("#,###").format(amount);
    }

    private String generateItemListString(List<BillingItemModel> items) {
        if (items == null || items.isEmpty()) {
            return "- 상세 내역 없음";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            BillingItemModel item = items.get(i);
            sb.append("- [")
              .append(item.getCategory())
              .append("] ")
              .append(item.getItemName())
              .append(": ")
              .append(formatMoney(item.getAmount()))
              .append("원\n");
        }
        return sb.toString().trim();
    }

    private String generateDiscountListString(List<BillingDiscountModel> discounts) {
        if (discounts == null || discounts.isEmpty()) return "- 할인 내역 없음";
        StringBuilder sb = new StringBuilder();
        for (BillingDiscountModel discount : discounts) {
            sb.append("- ")
              .append(discount.getDiscountName())
              .append(": -")
              .append(formatMoney(discount.getDiscountAmount()))
              .append("원\n");
        }
        return sb.toString().trim();
    }
}