package ru.udaltsov.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.configs.WebClientConfig;
import ru.udaltsov.application.services.telegram.messages.exceptions.UserRequestException;
import ru.udaltsov.models.Owner;
import ru.udaltsov.models.repositories.OwnerRepository;

import java.util.Map;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    private final WebClient userClient;

    @Autowired
    public OwnerService(
            OwnerRepository ownerRepository,
            WebClientConfig clientConfig) {
        this.ownerRepository = ownerRepository;
        userClient = clientConfig.webClientBuilder()
                .baseUrl("https://api.github.com/user")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
    }

    public Mono<Boolean> saveOwner(String chatId, String token) {
        return userClient
                .get()
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    if (!response.containsKey("login")) {
                        return Mono.error(new UserRequestException("Failed to request token"));
                    }

                    return ownerRepository
                            .getOwnerById(Long.parseLong(chatId))
                            .flatMap(owner -> Mono.just(true))
                            .switchIfEmpty(
                                    ownerRepository
                                            .addOwner(new Owner(Long.parseLong(chatId), response.get("login").toString()))
                                            .flatMap(ownersInserted -> {
                                                if (ownersInserted == 0) {
                                                    return Mono.just(false);
                                                }

                                                return Mono.just(true);
                                            })
                            );
                });
    }

    public Mono<String> findOwnerByChatId(String chatId) {
        return ownerRepository.getOwnerById(Long.parseLong(chatId))
                .map(Owner::owner);
    }

    public Mono<Long> findChatIdByOwnerName(String ownerName) {
        return ownerRepository.getOwnerByOwnerName(ownerName)
                .map(Owner::chatId);
    }
}
