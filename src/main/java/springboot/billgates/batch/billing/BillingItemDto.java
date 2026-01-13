package springboot.billgates.batch.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingItemDto {
    private Long memberId;
    private String name;
    private String phoneNumber; // phone -> phoneNumber 변경
    private String email;
    private Long sumAmount;     
}

