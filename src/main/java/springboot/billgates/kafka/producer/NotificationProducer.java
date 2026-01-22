package springboot.billgates.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.config.ChannelTopicMapper;
import springboot.billgates.kafka.dto.NotificationEvent;

// ChannelTopicMapper를 통해 채널별 토픽을 동적으로 결정하여 확장 가능
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final ChannelTopicMapper channelTopicMapper;

    public void send(NotificationEvent event) {
        try {
            // 채널에 맞는 토픽 결정 (확장 가능)
            String topic = channelTopicMapper.getTopic(event.getChannel());

            log.info("[PRODUCER] 발송 요청 - channel={}, topic={}, messageId={}", event.getChannel(), topic, event.getMessageId());

            // Kafka로 전송
            kafkaTemplate.send(topic, event.getMessageId().toString(), event);

        } catch (IllegalArgumentException e) {
            log.error("[PRODUCER] 발송 실패 - 등록되지 않은 채널: {}, messageId={}",
                    event.getChannel(), event.getMessageId(), e);
        } catch (Exception e) {
            log.error("[PRODUCER] 발송 실패 - channel={}, messageId={}, error={}",
                    event.getChannel(), event.getMessageId(), e.getMessage(), e);
        }
    }
}
