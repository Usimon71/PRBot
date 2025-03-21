package ru.udaltsov.models.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Webhook;

import java.util.UUID;

public interface WebhookRepository {

    Mono<Long> addWebhook(Webhook webhook);

    Mono<Long> deleteWebhook(Long webhookId);

    Flux<Webhook> findAllById(UUID integrationId);
}
