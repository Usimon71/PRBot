package ru.udaltsov.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class VaultService {

    private final WebClient tokenClient;
    private final WebClient secretClient;

    private final String TOKEN_URL = "https://auth.idp.hashicorp.com/oauth2/token";
    private final String SECRET_URL = "https://api.cloud.hashicorp.com/secrets/2023-11-28/organizations/6764204f-0025-4bfd-b71e-2f271e9b3b2b/projects/c0eef7cf-b426-4c1d-aa25-cc3906d55eba/apps/PRBotSecrets/secrets:open";

    @Autowired
    public VaultService(WebClient.Builder webClientBuilder) {
        this.tokenClient = webClientBuilder
                .baseUrl(TOKEN_URL)
                .build();
        this.secretClient = webClientBuilder
                .baseUrl(SECRET_URL)
                .build();
    }

    private Mono<String> getToken() {
        String body = "client_id=" + System.getenv("HCP_CLIENT_ID") +
                "&client_secret=" + System.getenv("HCP_CLIENT_SECRET") +
                "&grant_type=client_credentials" +
                "&audience=\"https://api.hashicorp.cloud\"";
        System.out.println(body);
        return tokenClient
                .post()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }

    public Mono<String> getSecrets() {
        return getToken().flatMap(token ->
                secretClient
                        .get()
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class)
        );
    }
}
