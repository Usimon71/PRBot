package ru.udaltsov.application.services.telegram.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.EncryptionService;
import ru.udaltsov.models.UserAccessToken;
import ru.udaltsov.models.repositories.UserAccessTokenRepository;

@Service
public class UserService {

    private final UserAccessTokenRepository tokenRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public UserService(
            UserAccessTokenRepository tokenRepository,
            EncryptionService encryptionService) {
        this.tokenRepository = tokenRepository;
        this.encryptionService = encryptionService;
    }

    public Mono<String> findUserToken(String chatId) {
        return tokenRepository.FindById(Long.parseLong(chatId))
                .map(UserAccessToken::token)
                .map(encryptionService::decrypt);
    }

    public Mono<SaveUserResult> saveUserToken(String chatId, String token) {
        String encryptedToken = encryptionService.encrypt(token);
        return tokenRepository
                .Add(new UserAccessToken(Long.parseLong(chatId), encryptedToken))
                .flatMap(rowsInserted -> {
                    if (rowsInserted == 0) {
                        return Mono.just(new SaveUserResult.Failure());
                    }

                    return Mono.just(new SaveUserResult.Success());
                });
    }
}
