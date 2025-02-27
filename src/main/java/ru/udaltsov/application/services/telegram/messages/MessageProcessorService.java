package ru.udaltsov.application.services.telegram.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.models.update.Message;
import ru.udaltsov.application.services.telegram.messages.commands.ConnectService;
import ru.udaltsov.application.services.telegram.messages.commands.IntegrationProviderService;

@Service
public class MessageProcessorService {

    private final ConnectService _connectService;

    private final IntegrationProviderService _integrationProviderService;

    @Autowired
    public MessageProcessorService(
            ConnectService connectService,
            IntegrationProviderService integrationProviderService) {
        _connectService = connectService;
        _integrationProviderService = integrationProviderService;
    }

    public Mono<ResponseEntity<String>> process(Message message) {
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

            return _integrationProviderService.sendRepositories(chatId);
        }

        return Mono.just(ResponseEntity.ok("Unsupported command: " + command));
    }
}
