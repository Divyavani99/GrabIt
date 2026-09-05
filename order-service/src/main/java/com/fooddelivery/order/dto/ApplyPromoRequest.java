package com.fooddelivery.order.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyPromoRequest(@NotBlank String promoCode) {}
