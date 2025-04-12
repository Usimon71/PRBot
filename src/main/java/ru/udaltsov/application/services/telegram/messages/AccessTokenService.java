package ru.udaltsov.application.services.telegram.messages;

import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.application.services.telegram.messages.exceptions.TokenRequestException;
import ru.udaltsov.application.services.telegram.messages.exceptions.UserRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccessTokenService {

    private final MessageSender messageSender;
    private final UserService userService;
    private final TokenService tokenService;
    private final OwnerService ownerService;

    @Autowired
    public AccessTokenService(
            MessageSender messageSender,
            UserService userService,
            TokenService tokenService,
            OwnerService ownerService) {
        this.messageSender = messageSender;
        this.userService = userService;
        this.tokenService = tokenService;
        this.ownerService = ownerService;
    }

    public Mono<ResponseEntity<String>> authorize(String code, String chatId) {
        return  userService.findUserToken(chatId)
                .flatMap(token -> sendSuccessMessage(chatId))
                .switchIfEmpty(handleNewAuthorization(code, chatId))
                .onErrorResume(TokenRequestException.class, ex -> sendFailureMessage(chatId))
                .onErrorResume(UserRequestException.class, ex -> sendFailureMessage(chatId));
    }

    private Mono<ResponseEntity<String>> handleNewAuthorization(String code, String chatId) {
        return tokenService.requestToken(code)
                .flatMap(accessToken -> userService.saveUserToken(chatId, accessToken)
                        .flatMap(saved -> {
                            if (!saved) {
                                return Mono.just(ResponseEntity.internalServerError()
                                        .body("Failed to save user access token"));
                            }
                            return ownerService.saveOwner(chatId, accessToken)
                                    .flatMap(savedOwner -> {
                                        if (!savedOwner) {
                                            return Mono.just(ResponseEntity
                                                    .internalServerError()
                                                    .body("Failed to save username"));
                                        }

                                        return sendSuccessMessage(chatId);
                                    });
                        }));
    }

    private Mono<ResponseEntity<String>> sendSuccessMessage(String chatId) {
        return messageSender.sendMessage(Long.parseLong(chatId), "Successful identification")
                .thenReturn(getRedirectResponse());
    }

    private Mono<ResponseEntity<String>> sendFailureMessage(String chatId) {
        return messageSender
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