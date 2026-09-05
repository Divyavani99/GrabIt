package com.fooddelivery.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    private UUID id; // == userId, one active cart per user

    @Column(nullable = false)
    private UUID restaurantId; // all cart items belong to one restaurant at a time

    private String promoCode;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Cart(UUID userId) {
        this.id = userId;
    }
}
