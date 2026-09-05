package com.fooddelivery.tracking.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);

    // Reacts to order-service's status changes (e.g. to stop tracking once DELIVERED/CANCELLED).
    @KafkaListener(topics = "order-status-events", groupId = "tracking-service-group",
                   properties = {"spring.json.value.default.type=java.util.Map"})
    public void onOrderStatusChanged(Map<String, Object> event) {
        log.info("Order status changed: {}", event);
    }
}
