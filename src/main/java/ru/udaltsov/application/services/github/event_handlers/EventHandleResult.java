package ru.udaltsov.application.services.github.event_handlers;

public sealed interface EventHandleResult {

    record Success(String result) implements EventHandleResult { }

    record EventNotSupported() implements EventHandleResult { }
}

