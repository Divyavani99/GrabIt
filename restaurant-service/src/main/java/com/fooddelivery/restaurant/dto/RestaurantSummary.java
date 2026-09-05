package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.Restaurant;

import java.util.UUID;

public record RestaurantSummary(
        UUID id,
        String name,
        String cuisine,
        Double rating,
        boolean isOpen,
        double distanceKm
) {
    public static RestaurantSummary of(Restaurant r, double distanceKm) {
        return new RestaurantSummary(r.getId(), r.getName(), r.getCuisine(), r.getRating(), r.isOpen(), distanceKm);
    }
}
