package springboot.billgates.domain.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    private Long memberId;      
    private String name;
    private String email;
    private String phoneNumber; 
}