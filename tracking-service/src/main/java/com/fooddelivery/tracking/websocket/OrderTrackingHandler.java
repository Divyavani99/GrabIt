package com.fooddelivery.tracking.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.tracking.model.DeliveryTrackingUpdate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles WS /v1/orders/{orderId}/track connections.
 * Keeps an in-memory registry of sessions per orderId (local to this instance);
 * cross-instance fan-out is handled by Redis pub/sub in TrackingPubSubListener.
 */
@Component
public class OrderTrackingHandler extends TextWebSocketHandler {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("/v1/orders/([^/]+)/track");

    private final Map<String, List<WebSocketSession>> sessionsByOrderId = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String orderId = extractOrderId(session);
        if (orderId == null) {
            closeQuietly(session, CloseStatus.BAD_DATA);
            return;
        }
        session.getAttributes().put("orderId", orderId);
        sessionsByOrderId.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String orderId = (String) session.getAttributes().get("orderId");
        if (orderId != null) {
            List<WebSocketSession> sessions = sessionsByOrderId.get(orderId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionsByOrderId.remove(orderId);
                }
            }
        }
    }

    /** Called by TrackingPubSubListener whenever a new update arrives for this order, on any instance. */
    public void broadcast(String orderId, DeliveryTrackingUpdate update) {
        List<WebSocketSession> sessions = sessionsByOrderId.get(orderId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(update));
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException ignored) {
            // best-effort push; the client can always fall back to the REST polling endpoint
        }
    }

    private String extractOrderId(WebSocketSession session) {
        Matcher m = ORDER_ID_PATTERN.matcher(session.getUri().getPath());
        return m.find() ? m.group(1) : null;
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ignored) {
        }
    }
}
