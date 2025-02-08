package ru.udaltsov.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.udaltsov.application.MessageSender;
import ru.udaltsov.models.IUserAccessTokenRepository;
import ru.udaltsov.models.UserAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccessTokenService {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenService.class);
    private final WebClient auth_client;

    private final WebClient repo_client;

    private final MessageSender messageSender;

    private final IUserAccessTokenRepository userAccessTokenRepository;

    @Autowired
    public AccessTokenService(
            WebClient.Builder webClientBuilder,
            MessageSender messageSender,
            IUserAccessTokenRepository userAccessTokenRepository) {
        String baseUrl = "https://github.com/login/oauth/access_token";
        this.auth_client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
        String repoUrl = "https://api.github.com/user/repos";
        this.repo_client = webClientBuilder
                .baseUrl(repoUrl)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
        this.messageSender = messageSender;
        this.userAccessTokenRepository = userAccessTokenRepository;
    }

    public Mono<ResponseEntity<String>> Authorize(String code, String chatId) {
        return userAccessTokenRepository.FindById(Long.parseLong(chatId))
                .flatMap(userAccessToken ->
                        sendRepos(Long.parseLong(chatId),
                                userAccessToken.token()))
                .switchIfEmpty(
                        auth_client
                                .post()
                                .body(BodyInserters
                                        .fromFormData("client_id", System.getenv("CLIENT_ID"))
                                        .with("client_secret", System.getenv("SECRET"))
                                        .with("code", code)
                                        .with("redirect_uri", System.getenv("REDIRECT_AUTH_URL")))
                                .retrieve()
                                .bodyToMono(Map.class)
                                .flatMap(responseBody -> {
                                    if (responseBody.containsKey("access_token")) {
                                        String accessToken = (String) responseBody.get("access_token");

                                        return userAccessTokenRepository
                                                .Add(new UserAccessToken(Long.parseLong(chatId), accessToken))
                                                .flatMap(rowsInserted -> {
                                                    if (rowsInserted == 0) {
                                                        return Mono.just(ResponseEntity
                                                                                .internalServerError()
                                                                                .body("Failed to save user access token"));
                                                    }

                                                    return sendRepos(Long.parseLong(chatId), accessToken);
                                                });
                                    }

                                    return Mono.just(ResponseEntity
                                                            .badRequest()
                                                            .body("Failed to retrieve access token"));
                                })
                );
    }

    private Mono<ResponseEntity<String>> sendRepos(Long chatId, String accessToken) {
        ResponseEntity<String> redirectResponse = ResponseEntity
                .status(302)
                .header("Location", "https://t.me/pull_requestbot")
                .build();
        return repo_client
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody_repo -> {
                    List<Map<String, Object>> repos;
                    try {
                        repos = new ObjectMapper().readValue(responseBody_repo, new TypeReference<>() {});
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                    String repoNames = repos.stream()
                            .map(repo -> (String) repo.get("name"))
                            .collect(Collectors.joining(", "));

                    // Send the message asynchronously
                    return messageSender.sendMessage("Repositories: " + repoNames, chatId);
                })
                .then(Mono.just(redirectResponse)); // Ensure the redirect happens immediately
    }
}