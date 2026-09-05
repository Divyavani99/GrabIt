package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.AddItemRequest;
import com.fooddelivery.order.dto.ApplyPromoRequest;
import com.fooddelivery.order.dto.CartResponse;
import com.fooddelivery.order.model.Cart;
import com.fooddelivery.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /v1/cart  (X-User-Id header injected by the gateway from the JWT)
    @GetMapping
    public CartResponse getCart(@RequestHeader("X-User-Id") UUID userId) {
        return CartResponse.from(cartService.getCart(userId));
    }

    // POST /v1/cart/items?restaurantId=...
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestHeader("X-User-Id") UUID userId,
                                                 @RequestParam UUID restaurantId,
                                                 @Valid @RequestBody AddItemRequest req) {
        Cart cart = cartService.addItem(userId, restaurantId, req);
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    // DELETE /v1/cart/items/{itemId}
    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@RequestHeader("X-User-Id") UUID userId,
                                    @PathVariable UUID itemId) {
        return CartResponse.from(cartService.removeItem(userId, itemId));
    }

    // POST /v1/cart/promo
    @PostMapping("/promo")
    public CartResponse applyPromo(@RequestHeader("X-User-Id") UUID userId,
                                    @jakarta.validation.Valid @RequestBody ApplyPromoRequest req) {
        return CartResponse.from(cartService.applyPromo(userId, req.promoCode()));
    }
}
