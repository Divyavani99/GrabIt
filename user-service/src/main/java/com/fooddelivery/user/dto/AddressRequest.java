package com.fooddelivery.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String city,
        @NotNull Double lat,
        @NotNull Double lng,
        String label
) {}
