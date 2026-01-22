package springboot.billgates.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
 메시지 채널과 Kafka 토픽 매핑을 관리하는 클래스
 새로운 채널을 추가할 때 이 클래스 수정

 확장 방법:
 1. registerChannel() 메서드에 새 채널-토픽 매핑 추가
 2. KafkaConfig에서 해당 토픽 생성
 3. MessageSender 구현체 작성
 4. Consumer 작성
 */
@Slf4j
@Component
public class ChannelTopicMapper {

    private final Map<String, String> channelToTopicMap;

    public ChannelTopicMapper() {
        this.channelToTopicMap = new HashMap<>();
        registerChannels();
        log.info("ChannelTopicMapper 초기화 완료 - 등록된 매핑: {}", channelToTopicMap);
    }

    // 채널-토픽 매핑을 등록
    private void registerChannels() {
        // 기존 채널
        channelToTopicMap.put("EMAIL", "notification-email");
        channelToTopicMap.put("SMS", "notification-sms");

        // 새로운 채널 추가 예시
         channelToTopicMap.put("KAKAO", "notification-kakao");
    }

    public String getTopic(String channel) {
        String topic = channelToTopicMap.get(channel);

        if (topic == null) {
            throw new IllegalArgumentException(
                    String.format("등록되지 않은 채널입니다: %s (등록된 채널: %s)",
                            channel, channelToTopicMap.keySet())
            );
        }

        return topic;
    }

    // 해당 채널이 등록되어 있는지 확인
    public boolean isRegistered(String channel) {
        return channelToTopicMap.containsKey(channel);
    }

     // 등록된 모든 채널을 반환
    public java.util.Set<String> getRegisteredChannels() {
        return channelToTopicMap.keySet();
    }
}
