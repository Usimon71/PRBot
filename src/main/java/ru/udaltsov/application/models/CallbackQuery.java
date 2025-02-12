package ru.udaltsov.application.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CallbackQuery {

    @JsonProperty("id")
    private int id;

    private User from;

    private Message message; // The original message associated with the callback

    @JsonProperty("chat_instance")
    private String chatInstance;

    private String data; // The callback data sent from inline keyboard

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getChatInstance() {
        return chatInstance;
    }

    public void setChatInstance(String chatInstance) {
        this.chatInstance = chatInstance;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
