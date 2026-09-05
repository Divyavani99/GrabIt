package com.fooddelivery.delivery.dto;

import jakarta.validation.constraints.NotBlank;

public record AvailabilityUpdateRequest(@NotBlank String status) {} // AVAILABLE | BUSY | OFFLINE
