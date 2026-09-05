package com.fooddelivery.delivery.event;

import java.time.Instant;
import java.util.UUID;

public record DeliveryAssignedEvent(
        UUID orderId,
        String deliveryPartnerId,
        String deliveryPartnerName,
        Instant occurredAt
) {}
