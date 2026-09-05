package com.fooddelivery.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddItemRequest(
        @NotNull UUID itemId,
        @NotNull @Min(1) Integer quantity,
        String customizations
) {}
