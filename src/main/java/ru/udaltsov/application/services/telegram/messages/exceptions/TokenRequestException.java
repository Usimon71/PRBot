package ru.udaltsov.application.services.telegram.messages.exceptions;

public class TokenRequestException extends RuntimeException {
    public TokenRequestException(String message) {
        super(message);
    }
}
