package com.fooddelivery.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Entity 3: FoodMenu — item_id, restaurant_id, name, price, image_url, category, customizations[]
@Entity
@Table(name = "food_menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodMenu {

    @Id
    @GeneratedValue
    private UUID id; // item_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    // e.g. STARTERS, MAIN_COURSE, DESSERT, BEVERAGE
    @Column(nullable = false)
    private String category;

    // e.g. "Extra cheese", "No onions", "Spicy" — options a customer can pick when adding to cart
    @ElementCollection
    @CollectionTable(name = "food_menu_customizations", joinColumns = @JoinColumn(name = "food_menu_id"))
    @Column(name = "customization")
    private List<String> customizations = new ArrayList<>();

    private boolean vegetarian;

    private boolean available = true;
}
