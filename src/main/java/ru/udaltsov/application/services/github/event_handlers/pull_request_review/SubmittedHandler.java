package ru.udaltsov.application.services.github.event_handlers.pull_request_review;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;
import ru.udaltsov.application.services.github.event_handlers.pull_request_review.submitted_handlers.SubmittedEventFactory;

public class SubmittedHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        String state = payload.get("review").get("state").asText();

        return new SubmittedEventFactory().getHandler(state).handleEvent(payload, chatId);
    }
}
