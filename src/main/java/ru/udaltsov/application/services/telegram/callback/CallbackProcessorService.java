package ru.udaltsov.application.services.telegram.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.application.models.CallbackQuery;

@Service
public class CallbackProcessorService {
    private final NewIntegrationService _newIntegrationService;

    private final MessageSender _messageSender;

    @Autowired
    public CallbackProcessorService(
            NewIntegrationService newIntegrationService,
            MessageSender messageSender) {
        _newIntegrationService = newIntegrationService;
        _messageSender = messageSender;
    }

    public Mono<ResponseEntity<String>> process(CallbackQuery callbackQuery) throws JsonProcessingException {
        if (callbackQuery.getData() == null) {
            return Mono.just(ResponseEntity.badRequest().body("No callback query data found"));
        }

        var decodeResult = CallbackDataDecoder.decode(callbackQuery.getData());

        if (!"i".equals(decodeResult.get("type"))){
            return Mono.just(ResponseEntity.badRequest().body("Invalid callback query"));
        }

        String integrationName = decodeResult.get("value");

        Long chatId = Long.parseLong(decodeResult.get("chatid"));

        return _newIntegrationService.SaveIntegration(chatId, integrationName)
                .flatMap(result -> {
                    if (result.getStatusCode().is2xxSuccessful()){
                        return _messageSender.answerCallback(callbackQuery.getId(), "Saved integration", false);
                    }

                    return _messageSender.answerCallback(callbackQuery.getId(), "Failed to save integration", false);
                });
    }
}
