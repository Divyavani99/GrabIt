package com.fooddelivery.order.kafka;

import com.fooddelivery.order.event.DeliveryAssignedEvent;
import com.fooddelivery.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeliveryEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventsConsumer.class);

    private final OrderRepository orderRepository;

    public DeliveryEventsConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Published by delivery-service once it assigns a driver to an order.
    @KafkaListener(topics = "delivery-events", groupId = "order-service-group",
                   containerFactory = "deliveryKafkaListenerContainerFactory")
    @Transactional
    public void onDeliveryAssigned(DeliveryAssignedEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            order.setDeliveryPartnerId(event.deliveryPartnerId());
            order.setDeliveryPartnerName(event.deliveryPartnerName());
            orderRepository.save(order);
        }, () -> log.warn("Received DeliveryAssignedEvent for unknown order {}", event.orderId()));
    }
}
