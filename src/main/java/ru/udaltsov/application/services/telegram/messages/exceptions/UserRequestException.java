package ru.udaltsov.application.services.telegram.messages.exceptions;

public class UserRequestException extends RuntimeException {
    public UserRequestException(String message) {
        super(message);
    }
}
