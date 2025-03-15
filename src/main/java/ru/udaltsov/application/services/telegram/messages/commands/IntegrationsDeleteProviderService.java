package ru.udaltsov.application.services.telegram.messages.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.models.repositories.IntegrationRepository;

@Service
public class IntegrationsDeleteProviderService {
    private final IntegrationRepository integrationRepository;
    private final MessageSender messageSender;

    @Autowired
    public IntegrationsDeleteProviderService(
            IntegrationRepository integrationRepository,
            MessageSender messageSender) {
        this.integrationRepository = integrationRepository;
        this.messageSender = messageSender;
    }

    public Mono<ResponseEntity<String>> deleteIntegration(Long chatId) {

        return integrationRepository.FindAllIntegrationsById(chatId)
                .collectList()
                .flatMap(integrationsList -> {
                    return messageSender.sendIntegrations(chatId, integrationsList);
                });
    }
}
