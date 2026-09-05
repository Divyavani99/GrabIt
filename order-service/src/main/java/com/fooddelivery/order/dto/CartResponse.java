package com.fooddelivery.order.dto;

import com.fooddelivery.order.model.Cart;
import com.fooddelivery.order.model.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        UUID restaurantId,
        List<CartItem> items,
        BigDecimal subtotal,
        String promoCode
) {
    public static CartResponse from(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), cart.getRestaurantId(), cart.getItems(), subtotal, cart.getPromoCode());
    }
}
