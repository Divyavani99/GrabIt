package com.fooddelivery.delivery.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic deliveryEventsTopic() {
        return TopicBuilder.name("delivery-events").partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic deliveryTrackingTopic() {
        return TopicBuilder.name("delivery-tracking").partitions(6).replicas(1).build();
    }
}
