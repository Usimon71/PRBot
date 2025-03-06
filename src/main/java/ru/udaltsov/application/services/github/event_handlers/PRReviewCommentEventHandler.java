package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.pull_request_review_comment.PRReviewCommentEventHandlerFactory;

public class PRReviewCommentEventHandler implements EventHandler {

    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        var action = payload.get("action").asText();
        var handler = new PRReviewCommentEventHandlerFactory().getHandler(action);
        if (handler == null) {
            return Mono.just(new EventHandleResult.EventNotSupported());
        }

        return handler.handleEvent(payload, chatId);
    }
}
