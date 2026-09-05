package com.fooddelivery.tracking.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A single location/status update for an order's delivery, as produced onto Kafka
 * by the delivery-partner mobile app / driver-assignment service.
 */
public record DeliveryTrackingUpdate(
        UUID orderId,
        String deliveryPartnerId,
        String deliveryPartnerName,
        double lat,
        double lng,
        Integer etaMinutes,
        DeliveryStatus status,
        Instant timestamp
) implements Serializable {}
