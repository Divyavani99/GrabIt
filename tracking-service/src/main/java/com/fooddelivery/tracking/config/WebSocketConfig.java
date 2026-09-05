package com.fooddelivery.tracking.config;

import com.fooddelivery.tracking.websocket.OrderTrackingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderTrackingHandler orderTrackingHandler;

    public WebSocketConfig(OrderTrackingHandler orderTrackingHandler) {
        this.orderTrackingHandler = orderTrackingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WS /v1/orders/{orderId}/track
        registry.addHandler(orderTrackingHandler, "/v1/orders/{orderId}/track")
                .setAllowedOrigins("*");
    }
}
