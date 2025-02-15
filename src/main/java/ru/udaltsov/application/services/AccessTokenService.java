package ru.udaltsov.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.models.Owner;
import ru.udaltsov.models.repositories.IOwnerRepository;
import ru.udaltsov.models.repositories.IUserAccessTokenRepository;
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

    private final WebClient _userClient;

    private final MessageSender messageSender;

    private final IUserAccessTokenRepository userAccessTokenRepository;

    private final IOwnerRepository _ownerRepository;

    @Autowired
    public AccessTokenService(
            WebClient.Builder webClientBuilder,
            MessageSender messageSender,
            IUserAccessTokenRepository userAccessTokenRepository,
            IOwnerRepository ownerRepository) {
        String baseUrl = "https://github.com/login/oauth/access_token";
        this.auth_client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
        this.messageSender = messageSender;
        this.userAccessTokenRepository = userAccessTokenRepository;
        _ownerRepository = ownerRepository;
        _userClient = webClientBuilder
                .baseUrl("https://api.github.com/user")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
    }

    public Mono<ResponseEntity<String>> Authorize(String code, String chatId) {
        ResponseEntity<String> redirectResponse = ResponseEntity
                .status(302)
                .header("Location", "https://t.me/pull_requestbot")
                .build();
        return userAccessTokenRepository.FindById(Long.parseLong(chatId))
                .flatMap(userAccessToken -> {
                    return messageSender
                            .sendMessage(Long.parseLong(chatId), "Successful identification")
                                        .then(Mono.just(redirectResponse));
                        })
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
                                                    return _userClient
                                                            .get()
                                                            .header("Authorization", "Bearer " + accessToken)
                                                            .retrieve()
                                                            .bodyToMono(Map.class)
                                                            .flatMap(map -> _ownerRepository
                                                                    .addOwner(new Owner(
                                                                            Long.parseLong(chatId),
                                                                            map.get("login").toString()))
                                                                    .flatMap(ownersInserted -> {
                                                                        if (ownersInserted == 0) {
                                                                            return Mono.just(ResponseEntity
                                                                                    .internalServerError()
                                                                                    .body("Failed to save username"));
                                                                        }
                                                                        return messageSender.sendMessage(Long.parseLong(chatId), "Successful identification")
                                                                                .then(Mono.just(redirectResponse));
                                                                    }));
                                                });
                                    }

                                    return Mono.just(ResponseEntity
                                                            .badRequest()
                                                            .body("Failed to retrieve access token"));
                                })
                );
    }
}