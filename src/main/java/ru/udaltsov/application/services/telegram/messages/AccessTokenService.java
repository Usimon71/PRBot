package ru.udaltsov.application.services.telegram.messages;

import ru.udaltsov.application.services.telegram.messages.exceptions.TokenRequestException;
import ru.udaltsov.application.services.telegram.messages.exceptions.UserRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
        return  _userService.findUserToken(chatId)
                .flatMap(token -> sendSuccessMessage(chatId))
                .switchIfEmpty(handleNewAuthorization(code, chatId))
                .onErrorResume(TokenRequestException.class, ex -> sendFailureMessage(chatId))
                .onErrorResume(UserRequestException.class, ex -> sendFailureMessage(chatId));
    }

    private Mono<ResponseEntity<String>> handleNewAuthorization(String code, String chatId) {
        return _tokenService.requestToken(code)
                .flatMap(accessToken -> _userService.saveUserToken(chatId, accessToken)
                        .flatMap(result -> {
                            if (result instanceof SaveUserResult.Failure) {
                                return Mono.just(ResponseEntity.internalServerError()
                                        .body("Failed to save user access token"));
                            }
                            return _ownerService.saveOwner(chatId, accessToken)
                                    .flatMap(saved -> {
                                        if (!saved) {
                                            return Mono.just(ResponseEntity
                                                    .internalServerError()
                                                    .body("Failed to save username"));
                                        }

                                        return sendSuccessMessage(chatId);
                                    });
                        }));
    }

    private Mono<ResponseEntity<String>> sendSuccessMessage(String chatId) {
        return _messageSender.sendMessage(Long.parseLong(chatId), "Successful identification")
                .thenReturn(getRedirectResponse());
    }

    private Mono<ResponseEntity<String>> sendFailureMessage(String chatId) {
        return _messageSender
                .sendMessage(Long.parseLong(chatId), "Failed to authorize")
                .thenReturn(getRedirectResponse());
    }

    private static ResponseEntity<String> getRedirectResponse() {
        return ResponseEntity
                .status(302)
                .header("Location", "https://t.me/pull_requestbot")
                .build();
    }
}