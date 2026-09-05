package com.fooddelivery.order.event;

// Mirrors payment-service's event — only the fields order-service needs.
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID paymentId,
        UUID orderId,
        String status, // SUCCESS | FAILED
        String gatewayResponse,
        Instant occurredAt
) {}
