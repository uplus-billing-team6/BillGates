package springboot.billgates.domain.billing.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.jdbc.core.JdbcTemplate;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.dto.BillingJoinRow;
import springboot.billgates.domain.billing.batch.model.DiscountPolicyModel;
import springboot.billgates.domain.billing.batch.model.BillingModel;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.domain.billing.batch.sql.BillingSqls;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BillingGroupReader implements ItemStreamReader<BillingPack> {
    private final ItemReader<BillingJoinRow> delegate; // DB에서 한 줄씩 읽어오는 진짜 Reader
    private final String billingMonth; // 파라미터
    private final JdbcTemplate jdbcTemplate;

    private BillingJoinRow cachedRow; // 다음 사람 데이터를 미리 읽었을 때 잠시 보관하는 변수

    public BillingGroupReader(ItemReader<BillingJoinRow> delegate, String billingMonth, JdbcTemplate jdbcTemplate) {
        this.delegate = delegate;
        this.billingMonth = billingMonth;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BillingPack read() throws Exception {
        // 1. 읽을 데이터가 없으면 끝
        if (cachedRow == null) {
            cachedRow = delegate.read(); // 첫 실행 시 로딩
            if (cachedRow == null) {
                return null; // 진짜 데이터 없음
            }
        }

        // 2. 현재 처리할 회원 ID 기준 잡기
        Long currentMemberId = cachedRow.getMemberId();
        String currentEmail = cachedRow.getEmail();
        String currentPhoneNumber = cachedRow.getPhoneNumber();
        boolean useDnd = cachedRow.isUseDnd();
        Time dndStartTime = cachedRow.getDndStartTime();
        Time dndEndTime = cachedRow.getDndEndTime();

        List<BillingItemModel> items = new ArrayList<>();

        // 3. 같은 회원이면 계속 읽어서 리스트에 담기 (Grouping)
        while (cachedRow != null && cachedRow.getMemberId().equals(currentMemberId)) {
            // 사용 내역 변환 및 추가
            items.add(BillingItemModel.builder()
                                      .category(cachedRow.getCategory())
                                      .itemName(cachedRow.getItemName())
                                      .amount(cachedRow.getAmount())
                                      .build());

            // 다음 줄 읽기
            cachedRow = delegate.read();
        }

        // [New] 회원의 보유 할인 정책 조회 (N+1 문제 같지만, Batch FetchSize 단위로 돌고 인덱스 타므로 괜찮음)
        List<DiscountPolicyModel> activePolicies = fetchMemberPolicies(currentMemberId);

        // 4. 회원이 바뀌었거나 데이터가 끝났으면, 지금까지 모은 걸 포장(Pack)해서 리턴
        BillingModel billing = BillingModel.builder()
                                   .memberId(currentMemberId)
                                   .billingMonth(billingMonth)
                                   .totalAmount(0L)
                                   .totalDiscountAmount(0L)
                                   .originalAmount(0L)
                                   .createdAt(LocalDateTime.now())
                                   .build();

        return BillingPack.builder()
                          .email(currentEmail)
                          .phoneNumber(currentPhoneNumber)
                          .useDnd(useDnd)
                          .dndStartTime(dndStartTime)
                          .dndEndTime(dndEndTime)
                          .billing(billing)
                          .items(items)
                          .discountPolicies(activePolicies)
                          .build();
    }

    private List<DiscountPolicyModel> fetchMemberPolicies(Long memberId) {
        return jdbcTemplate.query(BillingSqls.SELECT_MEMBER_POLICIES,
            (rs, rowNum) -> DiscountPolicyModel.builder()
                                               .policyId(rs.getLong("policy_id"))
                                               .name(rs.getString("name"))
                                               .discountType(rs.getString("discount_type"))
                                               .discountValue(rs.getLong("discount_value"))
                                               .targetCategory(rs.getString("target_category"))
                                               .build(),
            memberId
        );
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        log.info(">>> [BillingGroupReader] 데이터 읽기를 시작합니다. (Month: {})", billingMonth);

        if (this.delegate instanceof ItemStream) {
            ((ItemStream) this.delegate).open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (this.delegate instanceof ItemStream) {
            ((ItemStream) this.delegate).update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (this.delegate instanceof ItemStream) {
            ((ItemStream) this.delegate).close();
        }
    }
}
