package ru.udaltsov.application.services.github.event_handlers.pull_request_review.submitted_handlers;

import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.PullRequestEventHandler;

import java.util.HashMap;
import java.util.Map;

public class SubmittedEventFactory {
    private final Map<String, EventHandler> handlers = new HashMap<>();

    public SubmittedEventFactory() {
        handlers.put("approved", new ApprovedHandler());
        handlers.put("changes_requested", new ChangesRequestedHandler());
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
