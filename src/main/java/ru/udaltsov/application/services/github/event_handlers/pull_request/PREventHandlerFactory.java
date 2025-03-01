package ru.udaltsov.application.services.github.event_handlers.pull_request;

import ru.udaltsov.application.services.github.event_handlers.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class PREventHandlerFactory {

    private final Map<String, EventHandler> handlers = new HashMap<>();

    public PREventHandlerFactory() {
        handlers.put("opened", new OpenedHandler());
        handlers.put("closed", new ClosedHandler());
        handlers.put("reopened", new ReopenedHandler());
        handlers.put("edited", new EditedHandler());
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
