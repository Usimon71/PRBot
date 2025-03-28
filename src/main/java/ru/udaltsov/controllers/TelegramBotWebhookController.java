package ru.udaltsov.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.udaltsov.application.models.update.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.VaultService;
import ru.udaltsov.application.services.telegram.callback.CallbackProcessorService;
import ru.udaltsov.application.services.telegram.messages.MessageProcessorService;

@RestController
@RequestMapping("/webhook/{token}")
public class TelegramBotWebhookController {

    private final MessageProcessorService messageProcessorService;
    private final CallbackProcessorService callbackProcessorService;
    private final VaultService vaultService;

    @Autowired
    public TelegramBotWebhookController(
            MessageProcessorService messageProcessorService,
            CallbackProcessorService callbackProcessorService,
            VaultService vaultService
            ) {
        this.messageProcessorService = messageProcessorService;
        this.callbackProcessorService = callbackProcessorService;
        this.vaultService = vaultService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> handleWebhook(
            @PathVariable String token,
            @RequestBody Update update) throws JsonProcessingException {

        if (!token.equals(vaultService.getSecret("BOT_TOKEN"))) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
        }

        return processUpdate(update);
    }

    private Mono<ResponseEntity<String>> processUpdate(Update update) throws JsonProcessingException {
        System.out.println("Telegram webhook received!");

        if (update.hasMessage()) {
            var message = update.getMessage();

            System.out.println("Message received!");

            return messageProcessorService.process(message);
        }

        if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();

            System.out.println("Callback query received!");

            return callbackProcessorService.process(callbackQuery);
        }

        return Mono.just(ResponseEntity.ok("TG Webhook processed successfully!"));
    }
}
