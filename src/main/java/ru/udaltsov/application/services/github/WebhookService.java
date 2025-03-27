package ru.udaltsov.application.services.github;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.VaultService;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.OwnerRepository;
import ru.udaltsov.models.repositories.WebhookRepository;

import java.util.UUID;

@Service
public class WebhookService {

    private final OwnerRepository ownerRepository;
    private final WebClient githubClient;
    private final WebhookRepository webhookRepository;
    private final VaultService vaultService;

    @Autowired
    public WebhookService(
            OwnerRepository ownerRepository,
            WebClient.Builder webClientBuilder,
            WebhookRepository webhookRepository,
            VaultService vaultService) {
        this.ownerRepository = ownerRepository;
        githubClient = webClientBuilder
                .baseUrl("https://api.github.com/repos")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
        this.webhookRepository = webhookRepository;
        this.vaultService = vaultService;
    }

    public Mono<ResponseEntity<String>> sendWebhook(WebhookInfo webhookInfo) {
        return ownerRepository.getOwnerById(Long.parseLong(webhookInfo.chatId()))
                .flatMap(owner ->
                    githubClient
                            .post()
                            .uri(String.format("/%s/%s/hooks", owner.owner(), webhookInfo.repoName()))
                            .header("Authorization", "Bearer " + webhookInfo.token())
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/vnd.github.v3+json")
                            .bodyValue(getBodyConfig(webhookInfo.webhook()))
                            .exchangeToMono(response -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    return response.bodyToMono(JsonNode.class)
                                            .map(jsonNode -> jsonNode.get("id").asText())
                                            .map(ResponseEntity::ok);
                                }
                                return response.bodyToMono(String.class)
                                        .flatMap(e -> Mono.error(new WebhookSetupException("Error sending webhook: " + e)));
                            }))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    public Mono<Boolean> saveWebhook(Webhook webhook) {
        return webhookRepository.addWebhook(webhook)
                .flatMap(rowsInserted -> {
                    if (rowsInserted == 0) {
                        return Mono.just(false);
                    }

                    return Mono.just(true);
                })
        ;
    }

    public Mono<Boolean> hasWebhook(UUID integrationId, String webhookName) {
        return webhookRepository.findAllById(integrationId)
                .filter(webhook -> (webhookName.equals(webhook.webhook())))
                .hasElements();

    }

    public Mono<Boolean> deleteWebhook(Long webhookId, WebhookInfo webhookInfo) {
        return ownerRepository.getOwnerById(Long.parseLong(webhookInfo.chatId()))
                .flatMap(owner ->
                        githubClient
                            .delete()
                            .uri(String.format("/%s/%s/hooks/%s", owner.owner(), webhookInfo.repoName(), webhookId))
                            .header("Authorization", "Bearer " + webhookInfo.token())
                            .header("Accept", "application/vnd.github.v3+json")
                            .exchangeToMono(response -> response.statusCode().is2xxSuccessful() ?
                                    Mono.just(true)
                                    : Mono.just(false)))
                .flatMap(deleted -> deleted ?
                        webhookRepository
                                .deleteWebhook(webhookId)
                                .flatMap(rowsDeleted -> rowsDeleted > 0 ?
                                        Mono.just(true)
                                        : Mono.just(false))
                        : Mono.just(false));
    }

    private String getBodyConfig(String webhookName) {
        String targetUrl = System.getenv("SMEE_URL");
        String secret = vaultService.getSecret("WEBHOOK_SECRET");

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
