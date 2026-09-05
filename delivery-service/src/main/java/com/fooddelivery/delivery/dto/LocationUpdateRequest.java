package com.fooddelivery.delivery.dto;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(
        @NotNull Double lat,
        @NotNull Double lng,
        Integer etaMinutes,
        String status // ASSIGNED | HEADED_TO_RESTAURANT | PICKED_UP | EN_ROUTE | ARRIVED | DELIVERED
) {}
