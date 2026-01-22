package springboot.billgates.global.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplateProvider {

    private final JdbcTemplate jdbcTemplate;

    /**
     * ID로 템플릿 단건 조회
     */
    public TemplateDto getTemplateById(Long templateId) {
        String sql = "SELECT channel, title, body FROM MESSAGE_TEMPLATE WHERE template_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new TemplateDto(
                rs.getString("channel"),
                rs.getString("title"),
                rs.getString("body")
            ), templateId);
        } catch (EmptyResultDataAccessException e) {
            log.error(">>> [Provider] Template not found. ID: {}", templateId);
            throw new IllegalStateException("필수 템플릿이 존재하지 않습니다. ID=" + templateId);
        }
    }
}