package springboot.billgates.domain.billing.batch.job;

import org.springframework.batch.item.*;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.dto.BillingJoinRow;
import springboot.billgates.domain.billing.batch.model.BillingModel;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillingGroupReader implements ItemStreamReader<BillingPack> {
    private final ItemReader<BillingJoinRow> delegate; // DB에서 한 줄씩 읽어오는 진짜 Reader
    private BillingJoinRow cachedRow; // 다음 사람 데이터를 미리 읽었을 때 잠시 보관하는 변수
    private final String billingMonth; // 파라미터

    public BillingGroupReader(ItemReader<BillingJoinRow> delegate, String billingMonth) {
        this.delegate = delegate;
        this.billingMonth = billingMonth;
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

        List<BillingItemModel> items = new ArrayList<>();
        long totalAmount = 0;

        // 3. 같은 회원이면 계속 읽어서 리스트에 담기 (Grouping)
        while (cachedRow != null && cachedRow.getMemberId().equals(currentMemberId)) {
            // 사용 내역 변환 및 추가
            items.add(BillingItemModel.builder()
                                      .category(cachedRow.getCategory())
                                      .itemName(cachedRow.getItemName())
                                      .amount(cachedRow.getAmount())
                                      .build());

            totalAmount += cachedRow.getAmount();

            // 다음 줄 읽기
            cachedRow = delegate.read();
        }

        // 4. 회원이 바뀌었거나 데이터가 끝났으면, 지금까지 모은 걸 포장(Pack)해서 리턴

        // (금액 0원 체크는 여기서 해도 됨)
        if (totalAmount == 0) {
            // 0원이면 다음 사람 처리를 위해 재귀 호출하거나 null 리턴?
            // 배치 흐름상 null 리턴은 '배치 종료' 의미이므로,
            // 0원인 경우도 일단 Pack으로 넘기고 Writer에서 무시하는 게 안전함.
            // 하지만 여기서는 Writer 로직 유지를 위해 그대로 생성.
        }

        BillingModel billing = BillingModel.builder()
                                           .memberId(currentMemberId)
                                           .billingMonth(billingMonth)
                                           .totalAmount(totalAmount)
                                           .createdAt(LocalDateTime.now())
                                           .build();

        return BillingPack.builder()
                          .email(currentEmail)
                          .billing(billing)
                          .items(items)
                          .build();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
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
