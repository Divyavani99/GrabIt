package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.FoodMenu;

import java.util.List;
import java.util.Map;

public record MenuPage(
        Map<String, List<FoodMenu>> itemsByCategory,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
