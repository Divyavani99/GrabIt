package com.fooddelivery.tracking.controller;

import com.fooddelivery.tracking.dto.TrackingResponse;
import com.fooddelivery.tracking.service.TrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/delivery")
public class DeliveryTrackingController {

    private final TrackingService trackingService;

    public DeliveryTrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    // GET /v1/delivery/{orderId}/tracking
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<TrackingResponse> getTracking(@PathVariable UUID orderId) {
        var current = trackingService.getCurrent(orderId);
        if (current.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TrackingResponse.of(null));
        }
        return ResponseEntity.ok(TrackingResponse.of(current.get()));
    }
}
