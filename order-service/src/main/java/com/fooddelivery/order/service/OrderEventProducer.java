package com.fooddelivery.order.service;

import com.fooddelivery.order.event.OrderPlacedEvent;
import com.fooddelivery.order.event.OrderStatusChangedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String ORDER_STATUS_TOPIC = "order-status-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        // Keyed by orderId so all events for the same order land on the same partition, preserving order.
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.orderId().toString(), event);
    }

    public void publishStatusChanged(OrderStatusChangedEvent event) {
        kafkaTemplate.send(ORDER_STATUS_TOPIC, event.orderId().toString(), event);
    }
}
