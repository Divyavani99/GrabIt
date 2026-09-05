package com.fooddelivery.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDriverRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String vehicle,
        @NotNull Double lat,
        @NotNull Double lng
) {}
