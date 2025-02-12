package ru.udaltsov.application.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.udaltsov.application.CallbackDataDecoder;
import ru.udaltsov.application.models.Update;
import ru.udaltsov.application.services.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.IntegrationProviderService;
import ru.udaltsov.application.services.NewIntegrationService;

@RestController
@RequestMapping("/webhook/{token}")
public class TelegramBotWebhookController {

    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final ConnectService _connectService;

    private final IntegrationProviderService _integrationProviderService;

    private final NewIntegrationService _newIntegrationService;

    @Autowired
    public TelegramBotWebhookController(
            ConnectService connectService,
            IntegrationProviderService integrationProviderService,
            NewIntegrationService newIntegrationService) {
        _connectService = connectService;
        _integrationProviderService = integrationProviderService;
        _newIntegrationService = newIntegrationService;
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

                return _integrationProviderService.SendRepositories(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();

            if (callbackQuery.getData() == null) {
                return Mono.just(ResponseEntity.badRequest().body("No callback query data found"));
            }

            var decodeResult = CallbackDataDecoder.decode(callbackQuery.getData());

            if (!decodeResult.has("integration")) {
                return Mono.just(ResponseEntity.badRequest().body("Invalid callback query"));
            }

            String integration = decodeResult.get("integration").toString();

            if (!decodeResult.has("chatid")) {
                return Mono.just(ResponseEntity.badRequest().body("Invalid callback query"));
            }

            Long chatId = Long.parseLong(decodeResult.get("chatid").toString());

            return _newIntegrationService.SaveIntegration(chatId, integration)
                    .flatMap(result -> {
                        if (result == 0) {
                            return Mono
                                    .just(ResponseEntity.internalServerError()
                                            .body("Failed to save integration"));
                        }

                        return Mono
                                .just(ResponseEntity.ok("Successfully saved integration"));
                    });
        }


        return Mono.just(ResponseEntity.ok("TG Webhook processed successfully!"));
    }
}
