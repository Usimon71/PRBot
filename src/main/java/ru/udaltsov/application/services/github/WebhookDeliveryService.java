package ru.udaltsov.application.services.github;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.OwnerService;
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
        return ownerService.findChatIdByOwnerName(payload.get("issue").get("user").get("login").asText())
                .flatMap(chatId -> {
                    String message = "Hey, %s".formatted(payload.get("issue").get("user").get("login").asText())
                            + "!\nNew " + eventType + " for you.";
                    return messageSender.sendMessage(chatId, message);
                });
    }
}
