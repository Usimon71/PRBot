package ru.udaltsov.models.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Integration;

public interface IIntegrationRepository {

    Flux<Integration> FindAllIntegrationsById(Long chatId);

    Mono<Integration> FindIntegrationByIdAndName(Long chatId, String name);

    Mono<Long> DeleteIntegration(Integration integration);

    Mono<Long> AddIntegration(Integration integration);
}
