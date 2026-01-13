package springboot.billgates.domain.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DummyDataRequest {
    @NotNull(message = "memberCount는 필수 입력값입니다.")
    @Min(value = 0, message = "memberCount는 0 이상이어야 합니다.")
    private Integer memberCount;

    @NotNull(message = "usageCount는 필수 입력값입니다.")
    @Min(value = 0, message = "usageCount는 0 이상이어야 합니다.")
    private Integer usageCount;
}
