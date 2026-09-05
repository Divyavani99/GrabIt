package com.fooddelivery.tracking.dto;

import com.fooddelivery.tracking.model.DeliveryTrackingUpdate;

public record TrackingResponse(
        boolean found,
        DeliveryTrackingUpdate lastKnownUpdate
) {
    public static TrackingResponse of(DeliveryTrackingUpdate update) {
        return update == null ? new TrackingResponse(false, null) : new TrackingResponse(true, update);
    }
}
