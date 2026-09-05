package com.fooddelivery.payment.kafka;

import com.fooddelivery.payment.event.OrderPlacedEvent;
import com.fooddelivery.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private final PaymentService paymentService;

    public OrderPlacedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Auto-charges every newly placed order so order-service can move it to CONFIRMED/CANCELLED.
    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void onOrderPlaced(OrderPlacedEvent event) {
        paymentService.processForOrder(event.orderId(), event.total());
    }
}
