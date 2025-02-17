package ru.udaltsov.application.services.telegram.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.data_access.repositories.UserAccessTokenRepository;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.IIntegrationRepository;
import ru.udaltsov.models.repositories.IOwnerRepository;
import ru.udaltsov.models.repositories.IUserAccessTokenRepository;
import ru.udaltsov.models.repositories.IWebhookRepository;

@Service
public class NewWebhookService {

    private final WebClient _githubClient;

    private final IUserAccessTokenRepository _userAccessTokenRepository;

    private final IOwnerRepository _ownerRepository;

    private final IIntegrationRepository _integrationRepository;

    private final IWebhookRepository _webhookRepository;

    @Autowired
    NewWebhookService(
            WebClient.Builder webClientBuilder,
            IUserAccessTokenRepository userAccessTokenRepository,
            IOwnerRepository ownerRepository,
            IIntegrationRepository integrationRepository,
            IWebhookRepository webhookRepository) {
        _githubClient = webClientBuilder
                .baseUrl("https://api.github.com/repos")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
        _userAccessTokenRepository = userAccessTokenRepository;
        _ownerRepository = ownerRepository;
        _integrationRepository = integrationRepository;
        _webhookRepository = webhookRepository;
    }

    public Mono<ResponseEntity<String>> setupWebhook(Long chatId, String webhook, String repoName) {
        return _userAccessTokenRepository
                .FindById(chatId)
                .flatMap(userAccessToken ->
                        _ownerRepository
                                .getOwnerById(chatId)
                                .flatMap(owner ->
                                        _githubClient
                                                .post()
                                                .uri(String.format("/%s/%s/hooks", owner.owner(), repoName))
                                                .header("Authorization", "Bearer " + userAccessToken.token())
                                                .header("Content-Type", "application/json")
                                                .header("Accept", "application/vnd.github.v3+json")
                                                .bodyValue(getBodyConfig(webhook))
                                                .retrieve()
                                                .bodyToMono(String.class)
                                                .doOnError(Mono::error)
                                                .map(ResponseEntity::ok))
                                .switchIfEmpty(
                                        Mono.just(ResponseEntity.notFound().build())
                                ))
                .switchIfEmpty(
                        Mono.just(ResponseEntity.notFound().build())
                )
                .flatMap(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return _integrationRepository
                                .FindIntegrationByIdAndName(chatId, repoName)
                                .flatMap(integration -> _webhookRepository
                                        .addWebhook(new Webhook(integration.id(), webhook))
                                        .flatMap(rowsInserted -> {
                                            if (rowsInserted == 0) {
                                                return Mono.just(ResponseEntity.internalServerError().body("Failed to save webhook"));
                                            }

                                            return Mono.just(ResponseEntity.ok("Successfully saved integration"));
                                        }));
                    }

                    return Mono.just(response);
                }
                );
    }

    private String getBodyConfig(String webhookName) {
        String targetUrl = System.getenv("SMEE_URL");
        return """
        {
            "name": "web",
            "active": true,
            "events": ["%s"],
            "config": {
                "url": "%s",
                "content_type": "json",
                "insecure_ssl": "0"
            }
        }
        """.formatted(webhookName, targetUrl);
    }
}
