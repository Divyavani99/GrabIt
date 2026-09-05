package com.fooddelivery.order.service;

import com.fooddelivery.order.client.RestaurantClient;
import com.fooddelivery.order.dto.AddItemRequest;
import com.fooddelivery.order.dto.FoodMenuDto;
import com.fooddelivery.order.exception.OrderExceptions;
import com.fooddelivery.order.model.Cart;
import com.fooddelivery.order.model.CartItem;
import com.fooddelivery.order.repository.CartItemRepository;
import com.fooddelivery.order.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestaurantClient restaurantClient;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                        RestaurantClient restaurantClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restaurantClient = restaurantClient;
    }

    public Cart getCart(UUID userId) {
        return cartRepository.findById(userId).orElseGet(() -> new Cart(userId));
    }

    @Transactional
    public Cart addItem(UUID userId, UUID restaurantId, AddItemRequest req) {
        Cart cart = cartRepository.findById(userId).orElseGet(() -> new Cart(userId));

        // Enforce single-restaurant carts: switching restaurants clears the existing cart.
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(restaurantId)) {
            cart.getItems().clear();
        }
        cart.setRestaurantId(restaurantId);

        FoodMenuDto menuItem = restaurantClient.getMenuItem(restaurantId, req.itemId());
        if (menuItem == null || !menuItem.available()) {
            throw new OrderExceptions.ItemUnavailableException("Item unavailable: " + req.itemId());
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setItemId(req.itemId());
        item.setItemName(menuItem.name());
        item.setUnitPrice(menuItem.price());
        item.setQuantity(req.quantity());
        item.setCustomizations(req.customizations());
        cart.getItems().add(item);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart applyPromo(UUID userId, String promoCode) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new OrderExceptions.CartEmptyException("No cart found for user " + userId));
        cart.setPromoCode(promoCode);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(UUID userId, UUID cartItemId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new OrderExceptions.CartEmptyException("No cart found for user " + userId));
        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        return cartRepository.save(cart);
    }
}
