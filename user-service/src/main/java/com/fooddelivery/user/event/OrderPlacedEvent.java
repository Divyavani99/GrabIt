package com.fooddelivery.user.event;

// Mirrors order-service's OrderPlacedEvent — only the fields user-service needs.
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
