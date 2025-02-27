package ru.udaltsov.application.services.github;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.IOwnerRepository;
import ru.udaltsov.models.repositories.IWebhookRepository;

import java.util.UUID;

@Service
public class WebhookService {

    private final IOwnerRepository _ownerRepository;

    private final WebClient _githubClient;

    private final IWebhookRepository _webhookRepository;

    @Autowired
    public WebhookService(
            IOwnerRepository ownerRepository,
            WebClient.Builder webClientBuilder,
            IWebhookRepository webhookRepository) {
        _ownerRepository = ownerRepository;
        _githubClient = webClientBuilder
                .baseUrl("https://api.github.com/repos")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
        _webhookRepository = webhookRepository;
    }

    public Mono<ResponseEntity<String>> sendWebhook(WebhookInfo webhookInfo) {
        return _ownerRepository.getOwnerById(Long.parseLong(webhookInfo.chatId()))
                .flatMap(owner ->
                    _githubClient
                            .post()
                            .uri(String.format("/%s/%s/hooks", owner.owner(), webhookInfo.repoName()))
                            .header("Authorization", "Bearer " + webhookInfo.token())
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/vnd.github.v3+json")
                            .bodyValue(getBodyConfig(webhookInfo.webhook()))
                            .exchangeToMono(response -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    return response.bodyToMono(String.class).map(ResponseEntity::ok);
                                }
                                return response.bodyToMono(String.class)
                                        .flatMap(e -> Mono.error(new WebhookSetupException("Error sending webhook: " + e)));
                            }))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    public Mono<Boolean> saveWebhook(Webhook webhook) {
        return _webhookRepository.addWebhook(webhook)
                .flatMap(rowsInserted -> {
                    if (rowsInserted == 0) {
                        return Mono.just(false);
                    }

                    return Mono.just(true);
                })
        ;
    }

    public Mono<Boolean> hasWebhook(UUID integrationId, String webhookName) {
        return _webhookRepository.findAllById(integrationId)
                .filter(webhook -> (webhookName.equals(webhook.webhook())))
                .hasElements();

    }

    private String getBodyConfig(String webhookName) {
        String targetUrl = System.getenv("SMEE_URL");
        String secret = System.getenv("WEBHOOK_SECRET");

        return """
        {
            "name": "web",
            "active": true,
            "events": ["%s"],
            "config": {
                "url": "%s",
                "content_type": "json",
                "secret": "%s",
                "insecure_ssl": "0"
            }
        }
        """.formatted(webhookName, targetUrl, secret);
    }
}
