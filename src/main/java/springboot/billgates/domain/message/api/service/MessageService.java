package springboot.billgates.domain.message.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.message.api.dto.MessageDto;
import springboot.billgates.entity.Message;
import springboot.billgates.repository.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public Page<MessageDto> getMessages(int page, int size) {
        // 1. DB 조회 시작점(offset) 계산
        int offset = page * size;

        // 2. Repository의 Native Query 호출 (일부만 가져옴)
        List<Message> content = messageRepository.findPageNative(offset, size);

        // 3. 전체 개수 조회
        long total = messageRepository.countAll();

        // 4. Spring Data의 Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "messageId"));

        // 5. PageImpl 객체로 감싸서 반환 (이래야 프론트에서 totalPages 등을 알 수 있음)
        return new PageImpl<>(
                content.stream().map(this::toDto).toList(),
                pageable,
                total
        );
    }

    // DTO 변환 보조 메서드
    private MessageDto toDto(Message m) {
        return MessageDto.builder()
                .messageId(m.getMessageId())
                .memberId(m.getMemberId())
                .billingId(m.getBillingId())
                .channel(m.getChannel())
                .status(m.getStatus())
                .templateCode(m.getTemplateCode())
                .createdAt(m.getCreatedAt())
                .reservedAt(m.getReservedAt())
                .build();
    }
}