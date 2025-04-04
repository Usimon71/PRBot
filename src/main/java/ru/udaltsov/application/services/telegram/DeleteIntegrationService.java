package ru.udaltsov.application.services.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.WebhookInfo;
import ru.udaltsov.application.services.github.WebhookService;
import ru.udaltsov.application.services.telegram.messages.UserService;
import ru.udaltsov.models.repositories.IntegrationRepository;
import ru.udaltsov.models.repositories.WebhookRepository;

import java.util.UUID;

@Service
public class DeleteIntegrationService {

    private final WebhookService webhookService;
    private final WebhookRepository webhookRepository;
    private final UserService userService;
    private final IntegrationRepository integrationRepository;

    @Autowired
    public DeleteIntegrationService(
            WebhookService webhookService,
            WebhookRepository webhookRepository,
            UserService userService,
            IntegrationRepository integrationRepository) {
        this.webhookService = webhookService;
        this.webhookRepository = webhookRepository;
        this.userService = userService;
        this.integrationRepository = integrationRepository;
    }

    public Mono<Boolean> deleteIntegration(UUID integrationId, Long chatId) {
        return webhookRepository
                .findAllById(integrationId)
                .flatMap(webhook ->
                        userService.findUserToken(chatId.toString())
                                .flatMap(token ->
                                        integrationRepository
                                                .findIntegrationById(integrationId)
                                                .flatMap(integration ->
                                                        webhookService
                                                                .deleteWebhook(
                                                                        webhook.webhookId(),
                                                                        new WebhookInfo(
                                                                                chatId.toString(),
                                                                                webhook.webhook(),
                                                                                integration.repoName(),
                                                                                token
                                                                                )
                                                                ))
                                        ))
                .all(deleted -> deleted)
                .flatMap(deleted -> deleted ?
                        integrationRepository
                                .DeleteIntegrationById(integrationId)
                                .flatMap(rowsUpdated -> rowsUpdated > 0 ?
                                        Mono.just(true)
                                        : Mono.just(false))
                        : Mono.just(false));
    }
}
