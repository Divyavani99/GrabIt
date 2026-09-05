package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.dto.InitiatePaymentRequest;
import com.fooddelivery.payment.model.Payment;
import com.fooddelivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /v1/payments — manual/explicit charge (e.g. retrying a failed payment)
    @PostMapping
    public ResponseEntity<Payment> initiate(@Valid @RequestBody InitiatePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(req));
    }

    // GET /v1/payments/{paymentId}
    @GetMapping("/{paymentId}")
    public Payment getById(@PathVariable UUID paymentId) {
        return paymentService.getById(paymentId);
    }

    // GET /v1/payments/order/{orderId}
    @GetMapping("/order/{orderId}")
    public List<Payment> getByOrder(@PathVariable UUID orderId) {
        return paymentService.getByOrderId(orderId);
    }
}
