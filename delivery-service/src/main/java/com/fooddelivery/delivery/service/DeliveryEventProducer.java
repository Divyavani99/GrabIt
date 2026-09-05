package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.event.DeliveryAssignedEvent;
import com.fooddelivery.delivery.event.DeliveryTrackingUpdate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        kafkaTemplate.send("delivery-events", event.orderId().toString(), event);
    }

    public void publishTrackingUpdate(DeliveryTrackingUpdate update) {
        kafkaTemplate.send("delivery-tracking", update.orderId().toString(), update);
    }
}
