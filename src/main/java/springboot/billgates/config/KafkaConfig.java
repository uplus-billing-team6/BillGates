package springboot.billgates.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    /**
     * 이메일 발송용 Topic
     * - 이메일 알림 메시지를 전송하는 Topic
     * - Partition: 3개 (병렬 처리)
     * - Replication: 1 (개발 환경)
     */
    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name("notification-email")
                .partitions(12)
                .replicas(1)
                .build();
    }

    /**
     * SMS 발송용 Topic
     * - SMS 알림 메시지를 전송하는 Topic
     * - Partition: 3개 (병렬 처리)
     * - Replication: 1 (개발 환경)
     */
    @Bean
    public NewTopic smsTopic() {
        return TopicBuilder.name("notification-sms")
                .partitions(12)
                .replicas(1)
                .build();
    }
}