package ru.udaltsov.application.services.github.event_handlers;

public class EventMessageFormatter {
    public static String escapeMarkdownV2(String text) {
        if (text == null) return "";
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

    public static String extractRepoName(String fullRepoName) {
        if (fullRepoName == null || fullRepoName.isEmpty()) {
            return "Unknown Repository";
        }
        String[] parts = fullRepoName.split("/");
        return parts.length > 1 ? parts[1] : fullRepoName;
    }
}
