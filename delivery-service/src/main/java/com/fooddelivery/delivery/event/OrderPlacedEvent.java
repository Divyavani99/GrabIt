package com.fooddelivery.delivery.event;

// Mirrors order-service's event — only the fields delivery-service needs.
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
