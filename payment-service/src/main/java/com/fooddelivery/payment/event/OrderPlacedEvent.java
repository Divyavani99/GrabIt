package com.fooddelivery.payment.event;

// Mirrors order-service's event — only the fields payment-service needs.
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        UUID userId,
        UUID restaurantId,
        UUID addressId,
        BigDecimal total,
        Instant occurredAt
) {}
