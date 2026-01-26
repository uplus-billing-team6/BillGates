package springboot.billgates.domain.message.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.domain.message.api.dto.SystemConfigDto;
import springboot.billgates.entity.MessageTemplate;
import springboot.billgates.repository.MessageTemplateRepository;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final MessageTemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public TemplateDto getTemplate(String channel) {
        // DB의 channel 컬럼(EMAIL, SMS 등)으로 조회
        MessageTemplate template = templateRepository.findByChannel(channel)
                .orElseThrow(() -> new RuntimeException("해당 템플릿을 찾을 수 없습니다: " + channel));
        
        // TemplateDto 생성자에 맞게 필드 전달 (기존 엔티티의 body가 템플릿 내용)
        return new TemplateDto(
                template.getChannel(), 
                template.getTitle(), 
                template.getBody()
            );
    }

    @Transactional
    public void updateTemplate(String channel, String newBody) {
        MessageTemplate template = templateRepository.findByChannel(channel)
                .orElseThrow(() -> new RuntimeException("해당 템플릿을 찾을 수 없습니다: " + channel));
        
        // 기존 엔티티의 body 필드 수정
        template.setBody(newBody); 
        // @Data 어노테이션이 엔티티에 있으므로 setBody 사용 가능
    }

    public SystemConfigDto getConfig() {
        // 현재는 별도의 설정 DB가 없으므로 UI 사진에 있던 기본값 반환
        return SystemConfigDto.builder()
                .isReserved(true)
                .reserveTime("08:00:00")
                .build();
    }
}