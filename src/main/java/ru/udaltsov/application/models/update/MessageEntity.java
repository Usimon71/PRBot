package ru.udaltsov.application.models.update;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageEntity {
    private String type;

    private int offset;

    private int length;

    @JsonProperty("url")
    private String url; // Optional: Only present for "text_link" types

    @JsonProperty("user")
    private User user; // Optional: Only present for "text_mention" types

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

