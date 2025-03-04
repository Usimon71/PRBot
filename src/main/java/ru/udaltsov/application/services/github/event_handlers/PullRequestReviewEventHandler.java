package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public class PullRequestReviewEventHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        String action = payload.get("action").asText();

        if ("pull_request_review".equals(action)) {
            return null;
        }

        return null;
    }
}
