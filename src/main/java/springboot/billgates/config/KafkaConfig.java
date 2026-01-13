package springboot.billgates.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name("notification-email")
                .partitions(12)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic smsTopic() {
        return TopicBuilder.name("notification-sms")
                .partitions(12)
                .replicas(1)
                .build();
    }
}