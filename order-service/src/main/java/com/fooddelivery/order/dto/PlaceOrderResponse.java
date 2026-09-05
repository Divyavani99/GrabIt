package com.fooddelivery.order.dto;

import java.util.UUID;

public record PlaceOrderResponse(UUID orderId, String paymentLink) {}
