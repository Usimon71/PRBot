package ru.udaltsov.application.services.github.event_handlers.pull_request_review_comment;

import ru.udaltsov.application.services.github.event_handlers.*;

import java.util.HashMap;
import java.util.Map;

public class PRReviewCommentEventHandlerFactory {
    private final Map<String, EventHandler> handlers = new HashMap<>();

    public PRReviewCommentEventHandlerFactory() {
        handlers.put("created", new CreatedHandler());
        handlers.put("deleted", new DeletedHandler());
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
