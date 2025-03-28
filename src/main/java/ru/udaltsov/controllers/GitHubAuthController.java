package ru.udaltsov.controllers;

import ru.udaltsov.application.services.telegram.messages.AccessTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("auth/github")
public class GitHubAuthController {

    private final AccessTokenService accessTokenService;

    public GitHubAuthController(
            AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<String>> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String chatId) {

        return accessTokenService.authorize(code, chatId);
    }
}