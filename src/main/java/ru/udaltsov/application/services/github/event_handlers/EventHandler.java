package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public interface EventHandler {
    Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId);
}
