package ru.udaltsov.models.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Integration;

import java.util.UUID;

public interface IntegrationRepository {

    Flux<Integration> FindAllIntegrationsById(Long chatId);

    Mono<Integration> FindIntegrationByIdAndName(Long chatId, String name);

    Mono<Long> DeleteIntegrationById(UUID integrationId);

    Mono<Long> AddIntegration(Integration integration);

    Mono<Integration> findIntegrationById(UUID integrationId);
}
