package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.pull_request.PREventHandlerFactory;

public class PullRequestEventHandler implements EventHandler {
    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        String action = payload.get("action").asText();

        System.out.println("Action: " + action);

        EventHandler handler = new PREventHandlerFactory().getHandler(action);
        if (handler == null) {
            return Mono.just(new EventHandleResult.EventNotSupported());
        }
        return handler.handleEvent(payload, chatId);
    }
}