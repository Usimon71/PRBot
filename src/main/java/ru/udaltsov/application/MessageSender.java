package ru.udaltsov.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Mono<ResponseEntity<String>> sendMessage(Long chatId, String message) {
        String payload = "{\n" +
                "  \"chat_id\": \"" + chatId + "\",\n" +
                "  \"text\": \"" + message + "\"\n" +
                "}";
        return client
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> sendMessage(Long chatId, String message, List<String> replyOptions) {
        List<List<Map<String, String>>> keyboard = replyOptions.stream()
                .map(repo -> Collections.singletonMap("text", repo)) // Create a button for each repo
                .map(Collections::singletonList) // Wrap in a row (single button per row)
                .collect(Collectors.toList());

        Map<String, Object> replyMarkup = new HashMap<>();
        replyMarkup.put("keyboard", keyboard);
        replyMarkup.put("resize_keyboard", true);
        replyMarkup.put("one_time_keyboard", true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId); // Replace with actual chat ID
        payload.put("text", message);
        payload.put("reply_markup", replyMarkup);
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        // Convert to JSON
//        ObjectMapper objectMapper = new ObjectMapper();
//        String payload = "{\n" +
//                "  \"chat_id\": \"" + chatId + "\",\n" +
//                "  \"text\": \"" + message + "\"\n" +
//                "}";
        return client
                .post()
                .bodyValue(json)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }
}
