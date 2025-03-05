package ru.udaltsov.application.services.github.event_handlers.pull_request_review;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandleResult;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;
import ru.udaltsov.application.services.github.event_handlers.pull_request_review.submitted_handlers.SubmittedEventFactory;

public class SubmittedHandler implements EventHandler {
    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        String state = payload.get("review").get("state").asText();
        var handler = new SubmittedEventFactory().getHandler(state);
        if (handler == null) {
            return Mono.just(new EventHandleResult.EventNotSupported());
        }

        return handler.handleEvent(payload, chatId);
    }
}
