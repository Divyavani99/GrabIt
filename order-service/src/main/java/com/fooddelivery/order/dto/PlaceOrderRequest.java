package com.fooddelivery.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID cartId,
        @NotNull UUID addressId,
        @NotNull String paymentMethod,
        String promoCode
) {}
