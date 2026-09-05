package com.fooddelivery.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderExceptions.CartEmptyException.class)
    public ResponseEntity<Map<String, Object>> handleCartEmpty(OrderExceptions.CartEmptyException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(OrderExceptions.OrderNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.ItemUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUnavailable(OrderExceptions.ItemUnavailableException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OrderExceptions.DifferentRestaurantException.class)
    public ResponseEntity<Map<String, Object>> handleDifferentRestaurant(OrderExceptions.DifferentRestaurantException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", message
        ));
    }
}
