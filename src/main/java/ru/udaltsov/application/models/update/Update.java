package ru.udaltsov.application.models.update;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Update {
    @JsonProperty("update_id")
    private long updateId;

    private Message message;

    @JsonProperty("callback_query")
    private CallbackQuery callbackQuery;

    // Getters and setters
    public long getUpdateId() {
        return updateId;
    }

    public CallbackQuery getCallbackQuery() { return callbackQuery; }

    public Message getMessage() {
        return message;
    }

    public boolean hasMessage() {
        return message != null;
    }

    public boolean hasCallbackQuery() {
        return callbackQuery != null;
    }
}

