package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.InitiatePaymentRequest;
import com.fooddelivery.payment.event.PaymentCompletedEvent;
import com.fooddelivery.payment.exception.PaymentNotFoundException;
import com.fooddelivery.payment.model.Payment;
import com.fooddelivery.payment.model.PaymentStatus;
import com.fooddelivery.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;
    private final PaymentEventProducer eventProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentGatewayClient gatewayClient,
                           PaymentEventProducer eventProducer) {
        this.paymentRepository = paymentRepository;
        this.gatewayClient = gatewayClient;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Payment initiate(InitiatePaymentRequest req) {
        return process(req.orderId(), req.amount(), req.method());
    }

    /** Invoked automatically when order-service's OrderPlacedEvent arrives, using COD as the
     *  Kafka-driven default since the event doesn't carry the chosen payment method. A production
     *  system would include paymentMethod in OrderPlacedEvent and charge accordingly. */
    @Transactional
    public Payment processForOrder(UUID orderId, BigDecimal amount) {
        return process(orderId, amount, "UPI");
    }

    private Payment process(UUID orderId, BigDecimal amount, String method) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        PaymentGatewayClient.GatewayResult result = gatewayClient.charge(amount, method);

        payment.setStatus(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setGatewayResponse(result.rawResponse());
        payment.setUpdatedAt(Instant.now());
        payment = paymentRepository.save(payment);

        eventProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                payment.getId(), payment.getOrderId(), payment.getStatus().name(),
                payment.getGatewayResponse(), Instant.now()
        ));

        return payment;
    }

    public Payment getById(UUID id) {
        return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    public List<Payment> getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
