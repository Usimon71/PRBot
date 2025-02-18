package ru.udaltsov.application.services.telegram.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.UserAccessToken;
import ru.udaltsov.models.repositories.IUserAccessTokenRepository;

@Service
public class UserService {

    private final IUserAccessTokenRepository _tokenRepository;

    @Autowired
    public UserService(
            IUserAccessTokenRepository tokenRepository) {
        _tokenRepository = tokenRepository;
    }

    public Mono<String> findUserToken(String chatId) {
        return _tokenRepository.FindById(Long.parseLong(chatId))
                .map(UserAccessToken::token);
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
