package ru.udaltsov.application.services.telegram.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.exceptions.TokenRequestException;

import java.util.Map;

@Service
public class TokenService {

    private final WebClient _tokenClient;

    @Autowired
    public TokenService(WebClient.Builder clientBuilder) {
        String baseUrl = "https://github.com/login/oauth/access_token";
        _tokenClient = clientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<String> requestToken(String code) {
        return _tokenClient
                .post()
                .body(
                        BodyInserters.fromFormData("client_id", System.getenv("CLIENT_ID"))
                        .with("client_secret", System.getenv("SECRET"))
                        .with("code", code)
                        .with("redirect_uri", System.getenv("REDIRECT_AUTH_URL")))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    if (!response.containsKey("access_token")) {
                        return Mono.error(new TokenRequestException("Failed to retrieve access token"));
                    }

                    String token = response.get("access_token").toString();

                    return Mono.just(token);
                });
    }
}
