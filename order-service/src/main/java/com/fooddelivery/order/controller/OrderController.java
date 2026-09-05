package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.PlaceOrderRequest;
import com.fooddelivery.order.dto.PlaceOrderResponse;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /v1/orders
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestHeader("X-User-Id") UUID userId,
                                                           @Valid @RequestBody PlaceOrderRequest req) {
        PlaceOrderResponse response = orderService.placeOrder(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /v1/orders/{orderId}
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId);
    }
}
