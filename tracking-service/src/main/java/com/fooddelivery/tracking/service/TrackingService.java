package com.fooddelivery.tracking.service;

import com.fooddelivery.tracking.config.RedisConfig;
import com.fooddelivery.tracking.model.DeliveryTrackingUpdate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class TrackingService {

    private static final String CACHE_KEY_PREFIX = "tracking:current:";
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    private final RedisTemplate<String, DeliveryTrackingUpdate> redisTemplate;

    public TrackingService(RedisTemplate<String, DeliveryTrackingUpdate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Called by the Kafka consumer for every DeliveryTrackingUpdate. */
    public void ingest(DeliveryTrackingUpdate update) {
        String cacheKey = CACHE_KEY_PREFIX + update.orderId();
        redisTemplate.opsForValue().set(cacheKey, update, CACHE_TTL);

        // Fan out to every tracking-service instance via Redis pub/sub so WebSocket
        // clients connected to any instance get pushed the update in real time.
        redisTemplate.convertAndSend(RedisConfig.TRACKING_CHANNEL_PREFIX + update.orderId(), update);
    }

    public Optional<DeliveryTrackingUpdate> getCurrent(UUID orderId) {
        DeliveryTrackingUpdate update = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + orderId);
        return Optional.ofNullable(update);
    }
}
