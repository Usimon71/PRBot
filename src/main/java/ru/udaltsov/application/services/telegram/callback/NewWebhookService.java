package ru.udaltsov.application.services.telegram.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.WebhookInfo;
import ru.udaltsov.application.services.github.WebhookService;
import ru.udaltsov.application.services.github.WebhookSetupException;
import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.application.services.telegram.messages.UserService;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.IntegrationRepository;

import java.util.Objects;

@Service
public class NewWebhookService {

    private final IntegrationRepository integrationRepository;
    private final UserService userService;
    public final OwnerService ownerService;
    public final WebhookService webhookService;

    @Autowired
    NewWebhookService(
            IntegrationRepository integrationRepository,
            UserService userService,
            OwnerService ownerService,
            WebhookService webhookService) {
        this.integrationRepository = integrationRepository;
        this.userService = userService;
        this.ownerService = ownerService;
        this.webhookService = webhookService;
    }

    public Mono<ResponseEntity<String>> setupWebhook(String chatId, String webhook, String repoName) {
        return hasWebhook(chatId, repoName, webhook)
                .flatMap(has -> has ?
                        Mono.just(ResponseEntity.ok("Webhook already exists"))
                        : userService.findUserToken(chatId)
                            .flatMap(token -> ownerService.findOwnerByChatId(chatId)
                                    .flatMap(owner -> sendAndProcessWebhook(new WebhookInfo(chatId, webhook, repoName, token)))
                            )
                            .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("User token or owner not found")))
                            .onErrorResume(WebhookSetupException.class, this::handleSetupWebhookError)
                );
    }

    private Mono<ResponseEntity<String>> sendAndProcessWebhook(WebhookInfo webhookInfo) {
        return webhookService.sendWebhook(webhookInfo)
                .flatMap(response -> response.getStatusCode().is2xxSuccessful()
                        ? saveWebhookIntegration(
                                webhookInfo.chatId(),
                                webhookInfo.webhook(),
                                webhookInfo.repoName(),
                                Long.parseLong(Objects.requireNonNull(response.getBody())))
                        : Mono.just(ResponseEntity.internalServerError().body("Failed to setup webhook"))
                );
    }

    private Mono<ResponseEntity<String>> saveWebhookIntegration(String chatId, String webhook, String repoName, Long webhookId) {
        return integrationRepository.FindIntegrationByIdAndName(Long.parseLong(chatId), repoName)
                .flatMap(integration -> webhookService.saveWebhook(new Webhook(integration.id(), webhook, webhookId)))
                .flatMap(saved -> saved
                        ? Mono.just(ResponseEntity.ok("Successfully saved integration"))
                        : Mono.just(ResponseEntity.internalServerError().body("Failed to save webhook"))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.internalServerError().body("Integration not found")));
    }

    private Mono<ResponseEntity<String>> handleSetupWebhookError(WebhookSetupException ex) {
        return Mono.just(ResponseEntity.internalServerError().body("Error setting up webhook: " + ex.getMessage()));
    }

    private Mono<Boolean> hasWebhook(String chatId, String repoName, String webhookName) {
        return integrationRepository.FindIntegrationByIdAndName(Long.parseLong(chatId), repoName)
                .flatMap(integration -> webhookService.hasWebhook(integration.id(), webhookName))
                .switchIfEmpty(Mono.error(new RuntimeException("Integration not found")));
    }
}
