package ru.udaltsov.application.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Update {
    @JsonProperty("update_id")
    private long updateId;

    private Message message;

    // Getters and setters
    public long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(long updateId) {
        this.updateId = updateId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}

