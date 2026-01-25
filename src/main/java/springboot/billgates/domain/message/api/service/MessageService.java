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
    public Page<MessageDto> getMessages(int page, int size, String channel) { // 🔹 channel 파라미터 추가
        int offset = page * size;

        // 1. 필터 적용된 Native Query 호출
        List<Message> content = messageRepository.findPageNativeFiltered(offset, size, channel);

        // 2. 필터 적용된 전체 개수 조회
        long total = messageRepository.countFiltered(channel);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "messageId"));

        return new PageImpl<>(
                content.stream().map(this::toDto).toList(),
                pageable,
                total
        );
    }

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

