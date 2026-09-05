package com.fooddelivery.order.kafka;

import com.fooddelivery.order.event.PaymentCompletedEvent;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderStatus;
import com.fooddelivery.order.model.PaymentStatus;
import com.fooddelivery.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventsConsumer.class);

    private final OrderRepository orderRepository;

    public PaymentEventsConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Published by payment-service once it has processed (or failed) a payment for an order.
    @KafkaListener(topics = "payment-events", groupId = "order-service-group",
                   containerFactory = "paymentKafkaListenerContainerFactory")
    @Transactional
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            order.setPaymentId(event.paymentId().toString());
            if ("SUCCESS".equals(event.status())) {
                order.setPaymentStatus(PaymentStatus.SUCCESS);
                order.setStatus(OrderStatus.CONFIRMED);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setStatus(OrderStatus.CANCELLED);
            }
            orderRepository.save(order);
        }, () -> log.warn("Received PaymentCompletedEvent for unknown order {}", event.orderId()));
    }
}
