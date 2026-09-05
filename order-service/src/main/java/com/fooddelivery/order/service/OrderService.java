package com.fooddelivery.order.service;

import com.fooddelivery.order.dto.PlaceOrderRequest;
import com.fooddelivery.order.dto.PlaceOrderResponse;
import com.fooddelivery.order.event.OrderPlacedEvent;
import com.fooddelivery.order.event.OrderStatusChangedEvent;
import com.fooddelivery.order.exception.OrderExceptions;
import com.fooddelivery.order.model.*;
import com.fooddelivery.order.repository.CartRepository;
import com.fooddelivery.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private static final BigDecimal FLAT_DELIVERY_FEE = new BigDecimal("2.99");
    private static final BigDecimal PROMO_DISCOUNT_RATE = new BigDecimal("0.10"); // 10% off, demo only

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderEventProducer eventProducer;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                         OrderEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public PlaceOrderResponse placeOrder(UUID userId, PlaceOrderRequest req) {
        Cart cart = cartRepository.findById(req.cartId())
                .orElseThrow(() -> new OrderExceptions.CartEmptyException("Cart not found: " + req.cartId()));

        if (cart.getItems().isEmpty()) {
            throw new OrderExceptions.CartEmptyException("Cannot place an order with an empty cart");
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = (req.promoCode() != null && !req.promoCode().isBlank())
                ? subtotal.multiply(PROMO_DISCOUNT_RATE)
                : BigDecimal.ZERO;

        BigDecimal total = subtotal.subtract(discount).add(FLAT_DELIVERY_FEE);

        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(cart.getRestaurantId());
        order.setAddressId(req.addressId());
        order.setPaymentMethod(req.paymentMethod());
        order.setPromoCode(req.promoCode());
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setDeliveryFee(FLAT_DELIVERY_FEE);
        order.setTotal(total);
        order.setStatus(OrderStatus.PLACED);

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setItemId(ci.getItemId());
            oi.setItemName(ci.getItemName());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setCustomizations(ci.getCustomizations());
            order.getItems().add(oi);
        }

        order = orderRepository.save(order);

        // Payment link would normally come from a payment-service/PSP integration.
        String paymentLink = "https://pay.fooddelivery.example/checkout/" + order.getId();
        order.setPaymentLink(paymentLink);
        order = orderRepository.save(order);

        // Clear the cart now that the order has been placed.
        cart.getItems().clear();
        cart.setRestaurantId(null);
        cartRepository.save(cart);

        eventProducer.publishOrderPlaced(new OrderPlacedEvent(
                order.getId(), order.getUserId(), order.getRestaurantId(),
                order.getAddressId(), order.getTotal(), Instant.now()
        ));

        return new PlaceOrderResponse(order.getId(), paymentLink);
    }

    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExceptions.OrderNotFoundException("Order not found: " + orderId));
    }

    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        OrderStatus old = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        order = orderRepository.save(order);

        eventProducer.publishStatusChanged(new OrderStatusChangedEvent(orderId, old, newStatus, Instant.now()));
        return order;
    }
}
