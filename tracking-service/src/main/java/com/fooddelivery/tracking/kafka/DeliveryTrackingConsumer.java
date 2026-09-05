package com.fooddelivery.tracking.kafka;

import com.fooddelivery.tracking.model.DeliveryTrackingUpdate;
import com.fooddelivery.tracking.service.TrackingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryTrackingConsumer {

    private final TrackingService trackingService;

    public DeliveryTrackingConsumer(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    // Published by the delivery-partner app / driver-assignment service as the driver moves.
    @KafkaListener(topics = "delivery-tracking", groupId = "tracking-service-group")
    public void onLocationUpdate(DeliveryTrackingUpdate update) {
        trackingService.ingest(update);
    }
}
