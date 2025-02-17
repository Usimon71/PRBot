package ru.udaltsov.application.services.telegram.messages;

import org.springframework.http.HttpHeaders;
import ru.udaltsov.models.repositories.IOwnerRepository;
import ru.udaltsov.models.repositories.IUserAccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AccessTokenService {

    private final MessageSender _messageSender;

    private final UserService _userService;

    private final TokenService _tokenService;

    private final OwnerService _ownerService;

    @Autowired
    public AccessTokenService(
            MessageSender messageSender,
            UserService userService,
            TokenService tokenService,
            OwnerService ownerService) {
        _messageSender = messageSender;
        _userService = userService;
        _tokenService = tokenService;
        _ownerService = ownerService;
    }

    public Mono<ResponseEntity<String>> authorize(String code, String chatId) {
        ResponseEntity<String> redirectResponse = ResponseEntity
                .status(302)
                .header("Location", "https://t.me/pull_requestbot")
                .build();
        return  _userService.findUserToken(chatId)
                        .flatMap(token -> _messageSender
                                .sendMessage(Long.parseLong(chatId), "Successful identification")
                                .then(Mono.just(redirectResponse)))
                .switchIfEmpty(
                        _tokenService.requestToken(code)
                                .flatMap(accessToken ->
                                        _userService
                                                .saveUserToken(chatId, accessToken)
                                                .flatMap(result -> {
                                                    if (result instanceof SaveUserResult.Failure) {
                                                        return Mono.just(ResponseEntity
                                                                .internalServerError()
                                                                .body("Failed to save user access token"));
                                                    }

                                                    return _ownerService.saveOwner(chatId, accessToken)
                                                            .flatMap(saved -> {
                                                                if (!saved) {
                                                                    return Mono.just(ResponseEntity
                                                                            .internalServerError()
                                                                            .body("Failed to save username"));
                                                                }

                                                                return _messageSender
                                                                        .sendMessage(Long.parseLong(chatId), "Successful identification")
                                                                        .then(Mono.just(redirectResponse));
                                                            })
                                                            .onErrorResume(throwable -> {
                                                                if (throwable instanceof UserRequestException) {
                                                                    return _messageSender
                                                                            .sendMessage(Long.parseLong(chatId), "Failed to authorize");
                                                                }

                                                                return Mono.error(throwable);
                                                            });
                                                }))
                                .onErrorResume(throwable -> {
                                    if (throwable instanceof TokenRequestException) {
                                        return _messageSender
                                                .sendMessage(Long.parseLong(chatId), "Failed to authorize");
                                    }

                                    return Mono.error(throwable);
                                })
                );
    }
}