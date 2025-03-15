package ru.udaltsov.application.services.telegram.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.models.update.Message;
import ru.udaltsov.application.services.telegram.messages.commands.ConnectService;
import ru.udaltsov.application.services.telegram.messages.commands.IntegrationsDeleteProviderService;
import ru.udaltsov.application.services.telegram.messages.commands.IntegrationProviderService;

@Service
public class MessageProcessorService {

    private final ConnectService connectService;
    private final IntegrationProviderService integrationProviderService;
    private final IntegrationsDeleteProviderService deleteIntegrationService;

    @Autowired
    public MessageProcessorService(
            ConnectService connectService,
            IntegrationProviderService integrationProviderService,
            IntegrationsDeleteProviderService deleteIntegrationService) {
        this.connectService = connectService;
        this.integrationProviderService = integrationProviderService;
        this.deleteIntegrationService = deleteIntegrationService;
    }

    public Mono<ResponseEntity<String>> process(Message message) {
        var entities = message.getEntities();
        if (entities == null || entities.isEmpty()) {
            return Mono.just(ResponseEntity.ok("No entities found"));
        }

        int offset = entities.get(0).getOffset();
        int length = entities.get(0).getLength();
        String command = message.getText().substring(offset, offset + length);

        switch (command) {
            case "/connect" -> {
                Long chatId = message.getChat().getId();

                return connectService.ProvideAuthorizeLink(chatId);
            }
            case "/new_integration" -> {
                Long chatId = message.getChat().getId();

                return integrationProviderService.sendRepositories(chatId);
            }
            case "/delete_integration" -> {
                Long chatId = message.getChat().getId();

                return deleteIntegrationService.deleteIntegration(chatId);
            }
        }

        return Mono.just(ResponseEntity.ok("Unsupported command: " + command));
    }
}
