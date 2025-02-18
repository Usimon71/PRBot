package ru.udaltsov.application.services.github;

public record WebhookInfo(String chatId, String webhook, String repoName, String token) { }
