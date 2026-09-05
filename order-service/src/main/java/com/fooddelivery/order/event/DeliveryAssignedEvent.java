package com.fooddelivery.order.event;

// Mirrors delivery-service's event — only the fields order-service needs.
import java.time.Instant;
import java.util.UUID;

public record DeliveryAssignedEvent(
        UUID orderId,
        String deliveryPartnerId,
        String deliveryPartnerName,
        Instant occurredAt
) {}
