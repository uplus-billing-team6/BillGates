package springboot.billgates.kafka.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * MessageSender 팩토리 클래스
 * 채널명에 따라 적절한 MessageSender 구현체를 반환합니다.

 * 새로운 채널을 추가하려면:
 * 1. MessageSender 인터페이스를 구현하는 새 클래스 작성 (예: KakaoMessageSender)
 * 2. @Component 어노테이션 추가
 * 3. getChannel() 메서드에서 채널명 반환 (예: "KAKAO")
 * 4. 자동으로 Factory에 등록됨
 */
@Slf4j
@Component
public class MessageSenderFactory {

    private final Map<String, MessageSender> senderMap;

    /*
     Spring이 모든 MessageSender 구현체를 주입하여 초기화
     senders 등록된 모든 MessageSender 구현체 목록
     */
    public MessageSenderFactory(List<MessageSender> senders) {
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(
                        MessageSender::getChannel,  // Key: "EMAIL", "SMS"
                        Function.identity()          // Value: EmailMessageSender, SmsMessageSender
                ));

        log.info("MessageSenderFactory 초기화 완료 - 등록된 채널: {}", senderMap.keySet());
    }

    /*
     채널명에 해당하는 MessageSender를 반환합니다.
     channel 채널명 (EMAIL, SMS, KAKAO 등)
     MessageSender 구현체
     IllegalArgumentException 지원하지 않는 채널인 경우
     */
    public MessageSender getSender(String channel) {
        MessageSender sender = senderMap.get(channel);

        if (sender == null) {
            throw new IllegalArgumentException(
                    String.format("지원하지 않는 채널입니다: %s (지원 채널: %s)",
                            channel, senderMap.keySet())
            );
        }

        return sender;
    }

    // 지원하는 모든 채널 목록을 반환
    public List<String> getSupportedChannels() {
        return List.copyOf(senderMap.keySet());
    }

    // 해당 채널을 지원하는지 확인
    public boolean isSupported(String channel) {
        return senderMap.containsKey(channel);
    }
}
