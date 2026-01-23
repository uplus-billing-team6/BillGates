package springboot.billgates.global.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BillingMessageFormatter {
    private final ObjectMapper objectMapper;
    private final EncryptUtils encryptUtils;
    private static final Set<String> SENSITIVE_KEYS = Set.of("email", "phoneNumber", "recipient");

    /**
     * [제목 포맷팅]
     * TemplateDto를 받아서, title 부분의 {month}를 치환합니다.
     * @param templateDto DB에서 조회한 템플릿 객체
     * @param month       치환할 월 (예: "2025-04")
     * @return 완성된 제목 (예: "[BillGates] 2025-04 청구서")
     */
    public String formatTitle(TemplateDto templateDto, String month) {
        if (templateDto == null || templateDto.getTitle() == null) return "";

        String rawTitle = templateDto.getTitle();
        String targetMonth = (month == null) ? "" : month;

        return rawTitle.replace("{month}", targetMonth);
    }

    /**
     * [본문 포맷팅]
     * TemplateDto를 받아서, body 부분의 변수들을 Map 기반으로 치환합니다.
     * * @param templateDto DB에서 조회한 템플릿 객체
     * @param jsonBody   DB에 저장된 JSON 문자열
     * @return 완성된 본문
     */
    public String formatBody(TemplateDto templateDto, String jsonBody) {
        if (templateDto == null || templateDto.getBody() == null) return "";
        if (jsonBody == null || jsonBody.isBlank()) return templateDto.getBody();

        // JSON String -> Map 변환
        Map<String, Object> variables;
        try {
            variables = objectMapper.readValue(jsonBody, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            // 파싱 실패 시 템플릿 원본이라도 리턴
            return templateDto.getBody();
        }

        String result = templateDto.getBody();

        String recipient = "";
        String channel = templateDto.getChannel(); // "EMAIL" 또는 "SMS"

        if ("EMAIL".equalsIgnoreCase(channel)) {
            Object val = variables.get("email");
            if (val != null) {
                // 암호문 -> 복호화 -> 이메일 마스킹
                recipient = encryptUtils.maskEmail(encryptUtils.decrypt(String.valueOf(val)));
            }

        }
        else if ("SMS".equalsIgnoreCase(channel)) {
            Object val = variables.get("phoneNumber");
            if (val != null) {
                // 암호문 -> 복호화 -> 전화번호 마스킹
                recipient = encryptUtils.maskPhoneNumber(encryptUtils.decrypt(String.valueOf(val)));
            }
        }
        // 치환
        result = result.replace("{recipient}", recipient);

        // 나머지 변수 일괄 치환
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();

            if (SENSITIVE_KEYS.contains(key)) {
                continue;
            }

            Object valObj = entry.getValue();
            String value = (valObj == null) ? "" : String.valueOf(valObj);

            // [특수 로직] 리스트가 비어있을 때 처리
            if ("itemList".equals(key) || "discountList".equals(key)) {
                if (value.isBlank()) {
                    value = "- 상세 내역이 없습니다.";
                }
            }
            // 치환
            result = result.replace("{" + key + "}", value);
        }
        return result;
    }
}
