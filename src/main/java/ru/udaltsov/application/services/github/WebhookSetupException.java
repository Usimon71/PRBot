package ru.udaltsov.application.services.github;

public class WebhookSetupException extends RuntimeException {
    public WebhookSetupException(String message) {
        super(message);
    }
}
