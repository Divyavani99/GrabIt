package com.fooddelivery.tracking.config;

import com.fooddelivery.tracking.model.DeliveryTrackingUpdate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    public static final String TRACKING_CHANNEL_PREFIX = "tracking:";

    @Bean
    public RedisTemplate<String, DeliveryTrackingUpdate> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, DeliveryTrackingUpdate> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // Listener container backing pub/sub so every tracking-service instance can relay
    // updates to WebSocket clients connected to *that* instance, regardless of which
    // instance's Kafka consumer originally received the update.
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }
}
