package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.pull_request.PREventHandlerFactory;

public class PullRequestEventHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        String action = payload.get("action").asText();

        return new PREventHandlerFactory().getHandler(action).handleEvent(payload, chatId);
    }
}