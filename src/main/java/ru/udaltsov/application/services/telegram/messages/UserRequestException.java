package ru.udaltsov.application.services.telegram.messages;

public class UserRequestException extends RuntimeException {
    public UserRequestException(String message) {
        super(message);
    }
}
