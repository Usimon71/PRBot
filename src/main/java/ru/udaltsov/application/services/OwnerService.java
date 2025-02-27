package ru.udaltsov.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.exceptions.UserRequestException;
import ru.udaltsov.models.Owner;
import ru.udaltsov.models.repositories.IOwnerRepository;

import java.util.Map;

@Service
public class OwnerService {

    private final IOwnerRepository _ownerRepository;

    private final WebClient _userClient;

    @Autowired
    public OwnerService(
            IOwnerRepository ownerRepository,
            WebClient.Builder clientBuilder) {
        _ownerRepository = ownerRepository;
        _userClient = clientBuilder
                .baseUrl("https://api.github.com/user")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
    }

    public Mono<Boolean> saveOwner(String chatId, String token) {
        return _userClient
                .get()
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    if (!response.containsKey("login")) {
                        return Mono.error(new UserRequestException("Failed to request token"));
                    }

                    return _ownerRepository
                            .addOwner(new Owner(Long.parseLong(chatId), response.get("login").toString()))
                            .flatMap(ownersInserted -> {
                                if (ownersInserted == 0) {
                                    return Mono.just(false);
                                }

                                return Mono.just(true);
                            });
                });
    }

    public Mono<String> findOwnerByChatId(String chatId) {
        return _ownerRepository.getOwnerById(Long.parseLong(chatId))
                .map(Owner::owner);
    }

    public Mono<Long> findChatIdByOwnerName(String ownerName) {
        return _ownerRepository.getOwnerByOwnerName(ownerName)
                .map(Owner::chatId);
    }
}
