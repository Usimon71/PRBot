package ru.udaltsov.models;

import java.util.UUID;

public record Webhook(UUID integrationId, String webhook, Long webhookId) {
}
