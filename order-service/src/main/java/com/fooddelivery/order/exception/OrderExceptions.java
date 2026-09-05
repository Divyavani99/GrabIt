package com.fooddelivery.order.exception;

public class OrderExceptions {

    public static class CartEmptyException extends RuntimeException {
        public CartEmptyException(String message) { super(message); }
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) { super(message); }
    }

    public static class ItemUnavailableException extends RuntimeException {
        public ItemUnavailableException(String message) { super(message); }
    }

    public static class DifferentRestaurantException extends RuntimeException {
        public DifferentRestaurantException(String message) { super(message); }
    }
}
