package ru.udaltsov.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.configs.WebClientConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class VaultService {

    private final WebClient tokenClient;
    private final WebClient secretClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<Map<String, String>> secretsCache = new AtomicReference<>(new HashMap<>());

    private static final String TOKEN_URL = System.getenv("HCP_TOKEN_URL");
    private static final String SECRET_URL = System.getenv("HCP_SECRET_URL");

    @Autowired
    public VaultService(WebClientConfig webClientConfig) {
        this.tokenClient = webClientConfig.webClientBuilder().baseUrl(TOKEN_URL).build();
        this.secretClient = webClientConfig.webClientBuilder().baseUrl(SECRET_URL).build();
    }

    private Mono<String> getToken() {
        String body = "client_id=" + System.getenv("HCP_CLIENT_ID") +
                "&client_secret=" + System.getenv("HCP_CLIENT_SECRET") +
                "&grant_type=client_credentials" +
                "&audience=https://api.hashicorp.cloud";

        return tokenClient.post()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }

    public Mono<Map<String, String>> fetchSecrets() {
        return getToken().flatMap(token ->
                secretClient.get()
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(this::parseSecrets)
        );
    }

    private Map<String, String> parseSecrets(String response) {
        Map<String, String> secretsMap = new HashMap<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode secretsArray = root.get("secrets");

            if (secretsArray != null && secretsArray.isArray()) {
                for (JsonNode secretNode : secretsArray) {
                    String name = secretNode.get("name").asText();
                    String value = secretNode.get("static_version").get("value").asText();
                    secretsMap.put(name, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return secretsMap;
    }

    @PostConstruct
    public void initializeSecrets() {
        fetchSecrets().subscribe(secretsCache::set);
    }

    @Scheduled(fixedRate = 300000, initialDelay = 10000)
    public void refreshSecrets() {
        fetchSecrets().subscribe(secretsCache::set);
    }

    public String getSecret(String key) {
        return secretsCache.get().get(key);
    }

    public Map<String, String> getAllSecrets() {
        return Collections.unmodifiableMap(secretsCache.get());
    }
}
