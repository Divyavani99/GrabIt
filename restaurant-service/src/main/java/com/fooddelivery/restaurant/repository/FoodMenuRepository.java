package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.model.FoodMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodMenuRepository extends JpaRepository<FoodMenu, UUID> {
    Page<FoodMenu> findByRestaurantId(UUID restaurantId, Pageable pageable);
}
