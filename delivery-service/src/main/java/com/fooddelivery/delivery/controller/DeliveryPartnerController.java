package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.AvailabilityUpdateRequest;
import com.fooddelivery.delivery.dto.LocationUpdateRequest;
import com.fooddelivery.delivery.dto.RegisterDriverRequest;
import com.fooddelivery.delivery.model.AvailabilityStatus;
import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/delivery-partners")
public class DeliveryPartnerController {

    private final DeliveryPartnerService service;

    public DeliveryPartnerController(DeliveryPartnerService service) {
        this.service = service;
    }

    // POST /v1/delivery-partners  — onboard a new driver
    @PostMapping
    public ResponseEntity<DeliveryPartner> register(@Valid @RequestBody RegisterDriverRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(req));
    }

    // GET /v1/delivery-partners/{id}
    @GetMapping("/{id}")
    public DeliveryPartner getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // PATCH /v1/delivery-partners/{id}/availability
    @PatchMapping("/{id}/availability")
    public DeliveryPartner updateAvailability(@PathVariable UUID id,
                                               @Valid @RequestBody AvailabilityUpdateRequest req) {
        return service.updateAvailability(id, AvailabilityStatus.valueOf(req.status()));
    }

    // POST /v1/delivery-partners/{id}/orders/{orderId}/location — driver app pushes GPS pings
    @PostMapping("/{id}/orders/{orderId}/location")
    public DeliveryPartner updateLocation(@PathVariable UUID id,
                                           @PathVariable UUID orderId,
                                           @Valid @RequestBody LocationUpdateRequest req) {
        return service.updateLocation(id, orderId, req);
    }
}
