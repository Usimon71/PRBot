package ru.udaltsov.application.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Integration;
import ru.udaltsov.models.repositories.IIntegrationRepository;

@Service
public class NewIntegrationService {

    private final IIntegrationRepository _integrationRepository;

    public NewIntegrationService(IIntegrationRepository integrationRepository) {
        _integrationRepository = integrationRepository;
    }

    public Mono<Long> SaveIntegration(Long chatId, String name) {
        return _integrationRepository.AddIntegration(new Integration(chatId, name));
    }
}
