package com.fooddelivery.delivery.kafka;

import com.fooddelivery.delivery.event.OrderPlacedEvent;
import com.fooddelivery.delivery.exception.DeliveryExceptions;
import com.fooddelivery.delivery.service.DeliveryPartnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);

    private final DeliveryPartnerService deliveryPartnerService;

    public OrderPlacedConsumer(DeliveryPartnerService deliveryPartnerService) {
        this.deliveryPartnerService = deliveryPartnerService;
    }

    // Every newly placed order needs a driver assigned; in a real system this would likely
    // wait for payment confirmation first (consume payment-events instead/also).
    @KafkaListener(topics = "order-events", groupId = "delivery-service-group")
    public void onOrderPlaced(OrderPlacedEvent event) {
        try {
            deliveryPartnerService.assignDriverToOrder(event.orderId());
        } catch (DeliveryExceptions.NoAvailableDriverException ex) {
            log.warn("Could not assign a driver to order {}: {}", event.orderId(), ex.getMessage());
            // In production: retry with backoff, or push to a manual-assignment queue.
        }
    }
}
