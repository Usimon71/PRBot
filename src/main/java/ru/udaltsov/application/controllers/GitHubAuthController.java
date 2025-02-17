package ru.udaltsov.application.controllers;

import ru.udaltsov.application.services.telegram.messages.AccessTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("auth/github")
public class GitHubAuthController {

    private final AccessTokenService _accessTokenService;

    public GitHubAuthController(
            AccessTokenService accessTokenService) {
        _accessTokenService = accessTokenService;
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<String>> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String chatId) {

        return _accessTokenService.authorize(code, chatId);
    }
}