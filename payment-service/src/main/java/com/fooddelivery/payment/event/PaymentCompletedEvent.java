package com.fooddelivery.payment.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID paymentId,
        UUID orderId,
        String status, // SUCCESS | FAILED
        String gatewayResponse,
        Instant occurredAt
) {}
