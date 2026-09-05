package com.fooddelivery.order.event;

import com.fooddelivery.order.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant occurredAt
) {}
