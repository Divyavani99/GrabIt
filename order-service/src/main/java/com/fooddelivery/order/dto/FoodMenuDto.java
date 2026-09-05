package com.fooddelivery.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Mirrors the fields we need from restaurant-service's FoodMenu entity
public record FoodMenuDto(
        UUID id,
        String name,
        BigDecimal price,
        boolean available
) {}
