package springboot.billgates.global.utils;

import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;

import java.text.DecimalFormat;
import java.util.List;

@Component
public class BillingMessageFormatter {

    /**
     * 제목 변수 치환
     */
    public String formatTitle(String rawTitle, BillingPack pack) {
        if (rawTitle == null) return "";
        return rawTitle.replace("{month}", pack.getBilling().getBillingMonth());
    }

    /**
     * 본문 변수 치환 (금액, 리스트 포함)
     */
    public String formatBody(String rawBody, BillingPack pack) {
        if (rawBody == null) return "";

        String billingMonth = pack.getBilling().getBillingMonth();
        String totalAmount = formatMoney(pack.getBilling().getTotalAmount());
        String itemListString = generateItemListString(pack.getItems());

        return rawBody
            .replace("{month}", billingMonth)
            .replace("{totalAmount}", totalAmount)
            .replace("{itemList}", itemListString);
    }

    // --- 내부 Helper 메서드 (private) ---

    private String formatMoney(long amount) {
        return new DecimalFormat("#,###").format(amount);
    }

    private String generateItemListString(List<BillingItemModel> items) {
        if (items == null || items.isEmpty()) {
            return "- 상세 내역 없음";
        }

        StringBuilder sb = new StringBuilder();
        for (BillingItemModel item : items) {
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
}
