package ru.udaltsov.application.services.github.event_handlers.pull_request_review;

import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.pull_request.OpenedHandler;

import java.util.HashMap;
import java.util.Map;

public class PRReviewEventHandlerFactory {
    private final Map<String, EventHandler> handlers = new HashMap<>();

    public PRReviewEventHandlerFactory() {
        handlers.put("submitted", new OpenedHandler());
    }

    public EventHandler getEventHandler(String eventType) {
        return handlers.get(eventType);
    }
}
