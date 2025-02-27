package ru.udaltsov.application.services.telegram.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.application.models.update.CallbackQuery;
import ru.udaltsov.application.services.telegram.messages.commands.GitHubWebhooksProviderService;

import java.io.IOException;

@Service
public class CallbackProcessorService {

    private final NewIntegrationService _newIntegrationService;

    private final NewWebhookService _newWebhookService;

    private final MessageSender _messageSender;

    private final GitHubWebhooksProviderService _gitHubWebhooksProviderService;

    @Autowired
    public CallbackProcessorService(
            NewIntegrationService newIntegrationService,
            MessageSender messageSender,
            GitHubWebhooksProviderService gitHubWebhooksProviderService,
            NewWebhookService newWebhookService) {
        _newIntegrationService = newIntegrationService;
        _messageSender = messageSender;
        _gitHubWebhooksProviderService = gitHubWebhooksProviderService;
        _newWebhookService = newWebhookService;
    }

    public Mono<ResponseEntity<String>> process(CallbackQuery callbackQuery) throws JsonProcessingException {
        if (callbackQuery.getData() == null) {
            return Mono.just(ResponseEntity.badRequest().body("No callback query data found"));
        }

        var decodeResult = CallbackDataDecoder.decode(callbackQuery.getData());

        if ("i".equals(decodeResult.get("type"))){
            String integrationName = decodeResult.get("value");
            Long chatId = Long.parseLong(decodeResult.get("chatid"));

            return _newIntegrationService.saveIntegration(chatId, integrationName)
                    .flatMap(result -> {
                        if (result.getStatusCode().is2xxSuccessful()){
                            try {
                                return _messageSender.answerCallback(callbackQuery.getId(), "Saved integration", false)
                                        .then(_gitHubWebhooksProviderService.sendWebhooks(chatId, integrationName));
                            } catch (IOException e) {
                                return Mono.error(e);
                            }
                        }

                        return _messageSender.answerCallback(callbackQuery.getId(), "Failed to save integration", false);
                    });
        }

        if ("w".equals(decodeResult.get("type"))){
            String webhookName = decodeResult.get("value");
            String chatId = decodeResult.get("chatid");
            String repoName = decodeResult.get("repoName");

            return _newWebhookService.setupWebhook(chatId, webhookName, repoName)
                    .flatMap(result -> {
                        if (result.getStatusCode().is2xxSuccessful()){
                            return _messageSender.answerCallback(callbackQuery.getId(), "Done!", false);
                        }

                        return _messageSender.answerCallback(callbackQuery.getId(), "Failed", false);
                    }
                    );
        }
        return Mono.just(ResponseEntity.badRequest().body("Invalid callback query"));


    }
}
