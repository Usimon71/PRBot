package ru.udaltsov.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class MessageSender {
    protected static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final WebClient client;

    @Autowired
    public MessageSender(WebClient.Builder webClientBuilder) {
        String baseUrl = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
        this.client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Mono<String> sendMessage(String message, Long chatId) {
        String payload = "{\n" +
                "  \"chat_id\": \"" + chatId + "\",\n" +
                "  \"text\": \"" + message + "\"\n" +
                "}";
        return client
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)));
    }
}
