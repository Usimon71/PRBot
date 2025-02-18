package ru.udaltsov.application.services.telegram.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.WebhookInfo;
import ru.udaltsov.application.services.github.WebhookService;
import ru.udaltsov.application.services.github.WebhookSetupException;
import ru.udaltsov.application.services.telegram.messages.OwnerService;
import ru.udaltsov.application.services.telegram.messages.UserService;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.IIntegrationRepository;

@Service
public class NewWebhookService {

    private final IIntegrationRepository _integrationRepository;

    private final UserService _userService;

    public final OwnerService _ownerService;

    public final WebhookService _webhookService;

    @Autowired
    NewWebhookService(
            IIntegrationRepository integrationRepository,
            UserService userService,
            OwnerService ownerService,
            WebhookService webhookService) {
        _integrationRepository = integrationRepository;
        _userService = userService;
        _ownerService = ownerService;
        _webhookService = webhookService;
    }

    public Mono<ResponseEntity<String>> setupWebhook(String chatId, String webhook, String repoName) {
        return _userService.findUserToken(chatId)
                .flatMap(token -> _ownerService.findOwnerByChatId(chatId)
                        .flatMap(owner -> sendAndProcessWebhook(new WebhookInfo(chatId, webhook, repoName, token)))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("User token or owner not found")))
                .onErrorResume(WebhookSetupException.class, this::handleSetupWebhookError);
    }

    private Mono<ResponseEntity<String>> sendAndProcessWebhook(WebhookInfo webhookInfo) {
        return _webhookService.sendWebhook(webhookInfo)
                .flatMap(response -> response.getStatusCode().is2xxSuccessful()
                        ? saveWebhookIntegration(webhookInfo.chatId(), webhookInfo.webhook(), webhookInfo.repoName())
                        : Mono.just(ResponseEntity.internalServerError().body("Failed to setup webhook"))
                );
    }

    private Mono<ResponseEntity<String>> saveWebhookIntegration(String chatId, String webhook, String repoName) {
        return _integrationRepository.FindIntegrationByIdAndName(Long.parseLong(chatId), repoName)
                .flatMap(integration -> _webhookService.saveWebhook(new Webhook(integration.id(), webhook)))
                .flatMap(saved -> saved
                        ? Mono.just(ResponseEntity.ok("Successfully saved integration"))
                        : Mono.just(ResponseEntity.internalServerError().body("Failed to save webhook"))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.internalServerError().body("Integration not found")));
    }

    private Mono<ResponseEntity<String>> handleSetupWebhookError(WebhookSetupException ex) {
        return Mono.just(ResponseEntity.internalServerError().body("Error setting up webhook: " + ex.getMessage()));
    }
}
