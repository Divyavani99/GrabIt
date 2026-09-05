package com.fooddelivery.tracking.websocket;

import com.fooddelivery.tracking.config.RedisConfig;
import com.fooddelivery.tracking.model.DeliveryTrackingUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class TrackingPubSubListener implements MessageListener {

    private final RedisMessageListenerContainer container;
    private final OrderTrackingHandler trackingHandler;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public TrackingPubSubListener(RedisMessageListenerContainer container, OrderTrackingHandler trackingHandler) {
        this.container = container;
        this.trackingHandler = trackingHandler;
    }

    @PostConstruct
    public void subscribeToAllOrders() {
        // Subscribes this instance to every per-order channel so it can relay to any
        // locally-connected WebSocket client, no matter which instance ingested the update.
        container.addMessageListener(this, new PatternTopic(RedisConfig.TRACKING_CHANNEL_PREFIX + "*"));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String orderId = channel.substring(RedisConfig.TRACKING_CHANNEL_PREFIX.length());
            DeliveryTrackingUpdate update = objectMapper.readValue(message.getBody(), DeliveryTrackingUpdate.class);
            trackingHandler.broadcast(orderId, update);
        } catch (Exception ignored) {
            // malformed message; skip
        }
    }
}
