package ru.udaltsov.application.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.udaltsov.application.models.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.callback.CallbackProcessorService;
import ru.udaltsov.application.services.telegram.messages.MessageProcessorService;

@RestController
@RequestMapping("/webhook/{token}")
public class TelegramBotWebhookController {

    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final MessageProcessorService _messageProcessorService;

    private final CallbackProcessorService _callbackProcessorService;

    @Autowired
    public TelegramBotWebhookController(
            MessageProcessorService messageProcessorService,
            CallbackProcessorService callbackProcessorService
            ) {
        _messageProcessorService = messageProcessorService;
        _callbackProcessorService = callbackProcessorService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> handleWebhook(
            @PathVariable String token,
            @RequestBody Update update) throws JsonProcessingException {

        if (!token.equals(BOT_TOKEN)) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
        }

        return processUpdate(update);
    }

    private Mono<ResponseEntity<String>> processUpdate(Update update) throws JsonProcessingException {
        System.out.println("Telegram webhook received!");

        if (update.hasMessage()) {
            var message = update.getMessage();

            System.out.println("Message received!");

            return _messageProcessorService.process(message);
        }

        if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();

            System.out.println("Callback query received!");

            return _callbackProcessorService.process(callbackQuery);
        }

        return Mono.just(ResponseEntity.ok("TG Webhook processed successfully!"));
    }
}
