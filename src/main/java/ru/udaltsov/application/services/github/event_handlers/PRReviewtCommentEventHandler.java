package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public class PRReviewtCommentEventHandler implements EventHandler {

    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        return null;
    }
}
