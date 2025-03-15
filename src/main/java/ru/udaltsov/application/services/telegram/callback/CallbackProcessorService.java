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

    private final NewIntegrationService newIntegrationService;
    private final NewWebhookService newWebhookService;
    private final MessageSender messageSender;
    private final GitHubWebhooksProviderService gitHubWebhooksProviderService;
    private final DeleteIntegrationService deleteIntegrationService;

    @Autowired
    public CallbackProcessorService(
            NewIntegrationService newIntegrationService,
            MessageSender messageSender,
            GitHubWebhooksProviderService gitHubWebhooksProviderService,
            NewWebhookService newWebhookService,
            DeleteIntegrationService deleteIntegrationService) {
        this.newIntegrationService = newIntegrationService;
        this.messageSender = messageSender;
        this.gitHubWebhooksProviderService = gitHubWebhooksProviderService;
        this.newWebhookService = newWebhookService;
        this.deleteIntegrationService = deleteIntegrationService;
    }

    public Mono<ResponseEntity<String>> process(CallbackQuery callbackQuery) throws JsonProcessingException {
        if (callbackQuery.getData() == null) {
            return Mono.just(ResponseEntity.badRequest().body("No callback query data found"));
        }

        var decodeResult = CallbackDataDecoder.decode(callbackQuery.getData());

        if ("i".equals(decodeResult.get("type"))){
            String integrationName = decodeResult.get("value");
            Long chatId = Long.parseLong(decodeResult.get("chatid"));

            return newIntegrationService.saveIntegration(chatId, integrationName)
                    .flatMap(result -> {
                        if (result.getStatusCode().is2xxSuccessful()){
                            try {
                                return messageSender.answerCallback(callbackQuery.getId(), "Saved integration", false)
                                        .then(gitHubWebhooksProviderService.sendWebhooks(chatId, integrationName));
                            } catch (IOException e) {
                                return Mono.error(e);
                            }
                        }

                        return messageSender.answerCallback(callbackQuery.getId(), "Failed to save integration", false);
                    });
        }

        if ("w".equals(decodeResult.get("type"))){
            String webhookName = decodeResult.get("value");
            String chatId = decodeResult.get("chatid");
            String repoName = decodeResult.get("repoName");

            return newWebhookService.setupWebhook(chatId, webhookName, repoName)
                    .flatMap(result -> {
                        if (result.getStatusCode().is2xxSuccessful()){
                            return messageSender.answerCallback(callbackQuery.getId(), "Done!", false);
                        }

                        return messageSender.answerCallback(callbackQuery.getId(), "Failed", false);
                    }
                    );
        }

        if ("di".equals(decodeResult.get("type"))){
            String integrationId = decodeResult.get("value");
            String chatId = decodeResult.get("chatid");

            return deleteIntegrationService.deleteIntegration(integrationId)
                    .flatMap(deleted -> deleted ?
                            messageSender.answerCallback(callbackQuery.getId(), "Deleted integration", false)
                            : messageSender.answerCallback(callbackQuery.getId(), "Failed to delete", false));
        }
        return Mono.just(ResponseEntity.badRequest().body("Invalid callback query"));
    }
}
