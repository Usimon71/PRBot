package ru.udaltsov.models.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Webhook;

import java.util.UUID;

public interface IWebhookRepository {

    Mono<Long> addWebhook(Webhook webhook);

    Mono<Long> deleteWebhook(Webhook webhook);

    Flux<Webhook> findAllById(UUID id);
}
