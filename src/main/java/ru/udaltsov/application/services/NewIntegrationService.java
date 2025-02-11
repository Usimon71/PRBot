package ru.udaltsov.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.MessageSender;
import ru.udaltsov.data_access.UserAccessTokenRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewIntegrationService {
    private final MessageSender _messageSender;

    private final WebClient _repoClient;

    private final UserAccessTokenRepository _userAccessTokenRepository;

    @Autowired
    public NewIntegrationService(
            MessageSender messageSender,
            WebClient.Builder webClientBuilder,
            UserAccessTokenRepository userAccessTokenRepository) {
        _messageSender = messageSender;

        String repoUrl = "https://api.github.com/user/repos";
        _repoClient = webClientBuilder
                .baseUrl(repoUrl)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();

        _userAccessTokenRepository = userAccessTokenRepository;
    }

    public Mono<ResponseEntity<String>> SendRepositories(Long chatId) {
        return _userAccessTokenRepository.FindById(chatId)
                .flatMap(userAccessToken -> _repoClient
                        .get()
                        .header("Authorization", "Bearer " + userAccessToken)
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(responseBody_repo -> {
                            List<Map<String, Object>> repos;
                            try {
                                repos = new ObjectMapper().readValue(responseBody_repo, new TypeReference<>() {});
                            } catch (JsonProcessingException e) {
                                return Mono.error(e);
                            }
                            if (repos.isEmpty()) {
                                return _messageSender.sendMessage(chatId, "You have no repositories");
                            }

                            List<String> reposArr = repos.stream()
                                    .map(repo -> (String) repo.get("name"))
                                    .toList();

                            return _messageSender.sendMessage(chatId, "Select repository:", reposArr);
                        }))
                .switchIfEmpty(_messageSender.sendMessage(chatId, "You are not authenticated"));
    }
}
