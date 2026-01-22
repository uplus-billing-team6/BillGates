package springboot.billgates.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value; // 👈 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka; // 👈 추가 권장
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import springboot.billgates.kafka.dto.NotificationEvent;

@EnableKafka // 👈 Kafka Listener 활성화 어노테이션
@Configuration
public class KafkaConsumerConfig {

    // 1. 하드코딩 제거: yml 설정값을 가져오도록 변경
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, NotificationEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // 1. 주소 통일
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "billgates-default-group"); 
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // 2. 패키지 신뢰 설정 (기존 유지)
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        // 🚨 3. [핵심] 헤더 정보 무시 설정 (이게 없으면 매핑 에러 자주 발생)
        // "헤더에 적힌 클래스 정보는 무시하고, 내가 지정한 클래스로 매핑해라"
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); 
        
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50); 

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(NotificationEvent.class) // 명시적 타입 지정
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true); 
        factory.setConcurrency(6); 
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        return factory;
    }
}