package ru.udaltsov.application.controllers;

import ru.udaltsov.application.models.Update;
import ru.udaltsov.application.services.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.NewIntegrationService;

@RestController
@RequestMapping("/webhook/{token}")
public class TelegramBotWebhookController {

    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final ConnectService _connectService;

    private final NewIntegrationService _newIntegrationService;

    @Autowired
    public TelegramBotWebhookController(
            ConnectService connectService,
            NewIntegrationService newIntegrationService) {
        _connectService = connectService;
        _newIntegrationService = newIntegrationService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> handleWebhook(
            @PathVariable String token,
            @RequestBody Update update) {

        if (!token.equals(BOT_TOKEN)) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
        }

        return processUpdate(update);
    }

    private Mono<ResponseEntity<String>> processUpdate(Update update) {
        System.out.println("Webhook received!");

        var message = update.getMessage();

        var entities = message.getEntities();
        if (entities == null || entities.isEmpty()) {
            return Mono.just(ResponseEntity.ok("No entities found"));
        }

        int offset = entities.get(0).getOffset();
        int length = entities.get(0).getLength();
        String command = message.getText().substring(offset, offset + length);

        if ("/connect".equals(command)) {
            Long chatId = message.getChat().getId();

            return _connectService.ProvideAuthorizeLink(chatId);
        }

        if ("/new_integration".equals(command)) {
            Long chatId = message.getChat().getId();
            System.out.println("Got command");
            return _newIntegrationService.SendRepositories(chatId);
        }

        return Mono.just(ResponseEntity.ok("TG Webhook processed successfully!"));
    }
}
