package ru.udaltsov.application.services.telegram.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.repositories.IntegrationRepository;

import java.util.UUID;

@Service
public class DeleteIntegrationService {
    private final IntegrationRepository integrationRepository;

    @Autowired
    public DeleteIntegrationService(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
    }

    public Mono<Boolean> deleteIntegration(String integrationId) {
        UUID convertedId = UUID.fromString(integrationId);
        return integrationRepository.DeleteIntegrationById(convertedId)
                .flatMap(rowsUpdated -> rowsUpdated > 0 ?
                        Mono.just(true)
                        : Mono.just(false));
    }
}
