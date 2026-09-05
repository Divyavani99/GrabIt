package com.fooddelivery.delivery.event;

import java.time.Instant;
import java.util.UUID;

// Published onto "delivery-tracking" — consumed by tracking-service to power the
// WebSocket feed and the last-known-location cache.
public record DeliveryTrackingUpdate(
        UUID orderId,
        String deliveryPartnerId,
        String deliveryPartnerName,
        double lat,
        double lng,
        Integer etaMinutes,
        String status,
        Instant timestamp
) {}
