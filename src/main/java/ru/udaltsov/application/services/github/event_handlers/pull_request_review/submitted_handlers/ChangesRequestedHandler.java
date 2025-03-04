package ru.udaltsov.application.services.github.event_handlers.pull_request_review.submitted_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;

import java.util.logging.Handler;

public class ChangesRequestedHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        return null;
    }
}
