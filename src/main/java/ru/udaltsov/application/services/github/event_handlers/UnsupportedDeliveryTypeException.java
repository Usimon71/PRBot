package ru.udaltsov.application.services.github.event_handlers;

public class UnsupportedDeliveryTypeException extends Exception {
    public UnsupportedDeliveryTypeException(String message) {
        super(message);
    }
}
