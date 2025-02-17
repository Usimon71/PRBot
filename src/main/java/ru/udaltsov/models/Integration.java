package ru.udaltsov.models;

import java.util.UUID;

public record Integration(UUID id, Long chatId, String repoName) { }
