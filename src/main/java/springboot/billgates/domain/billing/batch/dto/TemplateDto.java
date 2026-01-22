package springboot.billgates.domain.billing.batch.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TemplateDto {
    private final String channel;
    private final String title;
    private final String body;
}
