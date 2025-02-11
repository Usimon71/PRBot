package ru.udaltsov.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        ObjectMapper mapper = new ObjectMapper();

        // Root JSON object
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", message);

        // Creating the keyboard layout
        ObjectNode replyMarkup = mapper.createObjectNode();
        ArrayNode keyboardArray = mapper.createArrayNode();

        for (int i = 0; i < replyOptions.size(); i++) {
            ArrayNode row = mapper.createArrayNode();
            row.add(replyOptions.get(i));
            if (i + 1 != replyOptions.size()) {
                row.add(replyOptions.get(i + 1));
                i += 1;
            }
            keyboardArray.add(row);
        }

        replyMarkup.set("keyboard", keyboardArray);
        replyMarkup.put("resize_keyboard", true);
        replyMarkup.put("one_time_keyboard", true);

        messageJson.set("reply_markup", replyMarkup);

        String payload;
        try {
            payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return client
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> removeKeyboard(Long chatId) {
        ObjectMapper mapper = new ObjectMapper();

        // Root JSON object
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", "Closing keyboard");

        ObjectNode replyMarkup = mapper.createObjectNode();
        replyMarkup.put("remove_keyboard", true);
        messageJson.set("reply_markup", replyMarkup);

        String payload;
        try {
            payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return client
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }
}
