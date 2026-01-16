//package springboot.billgates.config;
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//import springboot.billgates.kafka.dto.NotificationEvent;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class KafkaProducerConfig {
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Bean
//    public ProducerFactory<String, NotificationEvent> producerFactory() {
//        Map<String, Object> config = new HashMap<>();
//
//        // 기본 설정
//        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//
//
//        // 성능 최적화 설정
//        // 1. Batch 전송 (여러 메시지 묶어서 전송)
//        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // 16KB (기본값)
//        // 설명: 16KB가 차면 즉시 전송
//
//        // 2. Linger (대기 시간)
//        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);  // 10ms
//        // 설명: 배치가 안 차도 10ms 후 전송
//        // → 100만 건을 빠르게 전송하면서도 배치 효과
//
//        // 3. 압축 (네트워크 대역폭 절약)
//        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
//        // 설명: CPU는 약간 사용하지만 네트워크 효율 향상
//        // 옵션: none, gzip, snappy, lz4, zstd
//
//        // 4. 버퍼 메모리
//        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);  // 32MB (기본값)
//        // 설명: Producer가 사용할 수 있는 최대 메모리
//
//        // 안정성 설정
//        // 5. 멱등성 보장 (중복 방지)
//        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
//        // 설명: 네트워크 재시도 시 중복 메시지 방지
//        // → Producer가 자동으로 sequence number 관리
//
//        // 6. Acks (확인 수준)
//        config.put(ProducerConfig.ACKS_CONFIG, "1");  // Leader만 확인
//        // 설명:
//        // "0": 확인 안 함 (가장 빠름, 유실 가능)
//        // "1": Leader 확인 (균형) ← 추천
//        // "all": 모든 Replica 확인 (가장 느림, 가장 안전)
//
//        // 7. 요청 타임아웃
//        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);  // 30초
//        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);  // 2분
//        // 설명: 너무 오래 대기하지 않도록
//
//        return new DefaultKafkaProducerFactory<>(config);
//    }
//
//    @Bean
//    public KafkaTemplate<String, NotificationEvent> kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
//}






package springboot.billgates.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, NotificationEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // 기본
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 성능
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // 🔥 안정성 (중요)
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // ✅ 필수
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Timeout
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, NotificationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
