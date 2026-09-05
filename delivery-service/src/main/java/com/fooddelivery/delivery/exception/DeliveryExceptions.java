package com.fooddelivery.delivery.exception;

public class DeliveryExceptions {

    public static class DriverNotFoundException extends RuntimeException {
        public DriverNotFoundException(String message) { super(message); }
    }

    public static class NoAvailableDriverException extends RuntimeException {
        public NoAvailableDriverException(String message) { super(message); }
    }
}
