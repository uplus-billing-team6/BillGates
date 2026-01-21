package springboot.billgates.domain.billing.batch.job;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.domain.billing.batch.sql.BillingSqls;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.MessageTemplateProvider;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BillingCompositeWriter implements ItemWriter<BillingPack> {

    private final JdbcTemplate jdbcTemplate;
    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;

    private static final long TEMPLATE_CODE = 1L; // 고정 템플릿 ID
    private TemplateDto cachedTemplate; // 템플릿 캐싱 변수

    @Override
    public void write(Chunk<? extends BillingPack> chunk) {
        // 1. 템플릿 로딩 (최초 1회만 실행)
        if (cachedTemplate == null) {
            this.cachedTemplate = templateProvider.getTemplateById(TEMPLATE_CODE);
            log.info(">>> [Writer] Template Loaded: {}", cachedTemplate.getTitle());
        }

        log.info(">>> [Writer] Saving Chunk... (Size: {} items)", chunk.getItems().size());
        long startTime = System.currentTimeMillis();

        List<Object[]> billingArgs = new ArrayList<>();
        List<Object[]> itemArgs = new ArrayList<>();
        List<Object[]> messageArgs = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (BillingPack pack : chunk) {
            long billingId = TSID.fast().toLong();

            // 1. Billing 적재
            billingArgs.add(new Object[] {
                billingId,
                pack.getBilling().getMemberId(),
                pack.getBilling().getBillingMonth(),
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

            // 3. Message 적재
            long messageId = TSID.fast().toLong();
            LocalDateTime reservedAt = calculateReservedTime(now, pack);

            String finalTitle = messageFormatter.formatTitle(cachedTemplate.getTitle(), pack);
            String finalBody = messageFormatter.formatBody(cachedTemplate.getBody(), pack);

            messageArgs.add(new Object[] {
                messageId,
                pack.getBilling().getMemberId(),
                billingId,
                "EMAIL",
                "READY",
                Timestamp.valueOf(reservedAt),
                Timestamp.valueOf(now),
                TEMPLATE_CODE,
                finalTitle,
                finalBody,
                pack.getEmail(),
                pack.getPhoneNumber()
            });
        }

        // Bulk Insert 실행
        if (!billingArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING, billingArgs);
        if (!itemArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_ITEM, itemArgs);
        if (!messageArgs.isEmpty()) jdbcTemplate.batchUpdate(BillingSqls.INSERT_MESSAGE, messageArgs);

        long endTime = System.currentTimeMillis();
        log.info(">>> [Writer] Saved Complete. (Time taken: {}ms)", (endTime - startTime));
    }

    /**
     * [DnD 계산 로직]
     * 현재 시간이 사용자의 금지 시간대(Start ~ End)에 포함되면 -> End 시간으로 예약
     * 포함되지 않으면 -> 현재 시간(즉시 발송)
     */
    private LocalDateTime calculateReservedTime(LocalDateTime now, BillingPack pack) {
        // DnD 미사용자거나 정보가 없으면 즉시 return
        if (!pack.isUseDnd() || pack.getDndStartTime() == null || pack.getDndEndTime() == null) {
            return now;
        }

        LocalTime currentTime = now.toLocalTime();
        Time sqlStartTime = pack.getDndStartTime(); // java.sql.Time
        Time sqlEndTime = pack.getDndEndTime();     // java.sql.Time

        LocalTime start = sqlStartTime.toLocalTime();
        LocalTime end = sqlEndTime.toLocalTime();

        boolean inDndTime = false;
        LocalDateTime targetTime = now;

        // Case A: 자정을 걸치는 경우 (예: 22:00 ~ 07:00)
        if (start.isAfter(end)) {
            if (currentTime.isAfter(start) || currentTime.isBefore(end)) {
                inDndTime = true;

                // 현재가 22시, 23시라면 -> 내일 07시
                if (currentTime.isAfter(start)) {
                    targetTime = now.plusDays(1).withHour(end.getHour()).withMinute(end.getMinute()).withSecond(0);
                }
                // 현재가 01시, 06시라면 -> 오늘 07시
                else {
                    targetTime = now.withHour(end.getHour()).withMinute(end.getMinute()).withSecond(0);
                }
            }
        }
        // Case B: 당일 시간대 (예: 09:00 ~ 12:00) - 보통 드물지만 처리
        else {
            if (currentTime.isAfter(start) && currentTime.isBefore(end)) {
                inDndTime = true;
                // 오늘 끝나는 시간으로 설정
                targetTime = now.withHour(end.getHour()).withMinute(end.getMinute()).withSecond(0);
            }
        }
        return inDndTime ? targetTime : now;
    }
}