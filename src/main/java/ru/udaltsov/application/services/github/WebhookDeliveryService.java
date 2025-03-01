package ru.udaltsov.application.services.github;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventHandlerFactory;
import ru.udaltsov.application.services.telegram.messages.MessageSender;

@Service
public class WebhookDeliveryService {

    private final OwnerService ownerService;
    private final MessageSender messageSender;

    @Autowired
    public WebhookDeliveryService(
            OwnerService ownerService,
            MessageSender messageSender) {
        this.ownerService = ownerService;
        this.messageSender = messageSender;
    }

    public Mono<ResponseEntity<String>> process(JsonNode payload, String eventType) {
        return ownerService.findChatIdByOwnerName(payload.get("repository").get("owner").get("login").asText())
                .flatMap(chatId -> {
                    EventHandler handler = new EventHandlerFactory().getHandler(eventType);
                    return handler.handleEvent(payload, chatId)
                            .flatMap(message -> messageSender.sendMessage(chatId, message));
                });
    }
}
