package ru.udaltsov.application.services.github.event_handlers;

import java.util.HashMap;
import java.util.Map;

public class EventHandlerFactory {
    private final Map<String, EventHandler> handlers = new HashMap<>();

    public EventHandlerFactory() {
        handlers.put("issues", new IssueEventHandler());
        handlers.put("pull_request", new PullRequestEventHandler());
        handlers.put("issue_comment", new IssueCommentEventHandler());
        handlers.put("pull_request_review", new PullRequestReviewEventHandler());
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
