package ru.udaltsov.application.services.telegram.messages.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.models.repositories.UserAccessTokenRepository;

import java.util.*;

@Service
public class IntegrationProviderService {

    private final MessageSender messageSender;
    private final WebClient repoClient;
    private final UserAccessTokenRepository userAccessTokenRepository;

    @Autowired
    public IntegrationProviderService(
            MessageSender messageSender,
            WebClient.Builder webClientBuilder,
            UserAccessTokenRepository userAccessTokenRepository) {
        this.messageSender = messageSender;

        String repoUrl = "https://api.github.com/user/repos";
        repoClient = webClientBuilder
                .baseUrl(repoUrl)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();

        this.userAccessTokenRepository = userAccessTokenRepository;
    }

    public Mono<ResponseEntity<String>> sendRepositories(Long chatId) {
        return userAccessTokenRepository.FindById(chatId)
                .flatMap(userAccessToken ->
                   repoClient
                        .get()
                           .uri(uriBuilder -> uriBuilder.queryParam("type", "owner").build())
                        .header("Authorization", "Bearer " + userAccessToken.token())
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(responseBody_repo -> {
                            List<Map<String, Object>> repos;
                            try {
                                repos = new ObjectMapper().readValue(responseBody_repo, new TypeReference<>() {
                                });
                            } catch (JsonProcessingException e) {
                                return Mono.error(e);
                            }
                            if (repos.isEmpty()) {
                                return messageSender.sendMessage(chatId, "You have no repositories");
                            }

                            List<String> reposArr = repos.stream()
                                    .map(repo -> (String) repo.get("name"))
                                    .toList();

                            return messageSender.sendMessage(chatId, "Select repository:", reposArr, "i");
                        }))
                .switchIfEmpty(messageSender.sendMessage(chatId, "You are not authenticated"));
    }
}
