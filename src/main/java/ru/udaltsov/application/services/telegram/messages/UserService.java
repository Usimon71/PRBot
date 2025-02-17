package ru.udaltsov.application.services.telegram.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.UserAccessToken;
import ru.udaltsov.models.repositories.IOwnerRepository;
import ru.udaltsov.models.repositories.IUserAccessTokenRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final WebClient _userClient;

    private final IOwnerRepository _ownerRepository;

    private final IUserAccessTokenRepository _tokenRepository;

    @Autowired
    public UserService(
            WebClient.Builder webClientBuilder,
            IOwnerRepository ownerRepository,
            IUserAccessTokenRepository tokenRepository) {
        _userClient = webClientBuilder
                .baseUrl("https://api.github.com/user")
                .defaultHeader(HttpHeaders.USER_AGENT, "PRBot")
                .build();
        _ownerRepository = ownerRepository;
        _tokenRepository = tokenRepository;
    }

    public Mono<Optional<String>> findUserToken(String chatId) {
        return _tokenRepository.FindById(Long.parseLong(chatId))
                .map(token -> Optional.of(token.token()))
                .defaultIfEmpty(Optional.empty());
    }

    public Mono<SaveUserResult> saveUserToken(String chatId, String token) {
        return _tokenRepository
                .Add(new UserAccessToken(Long.parseLong(chatId), token))
                .flatMap(rowsInserted -> {
                    if (rowsInserted == 0) {
                        return Mono.just(new SaveUserResult.Failure());
                    }

                    return Mono.just(new SaveUserResult.Success());
                });
    }
}
