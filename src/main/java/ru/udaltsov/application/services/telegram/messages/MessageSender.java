package ru.udaltsov.application.services.telegram.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.List;

@Service
public class MessageSender {
    protected static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    private final WebClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public MessageSender(WebClient.Builder webClientBuilder) {
        String baseUrl = "https://api.telegram.org/bot" + BOT_TOKEN;
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
                .uri("/sendMessage")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> sendMessage(Long chatId, String message, List<String> replyOptions, String replyType) {
        // Root JSON object
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", message);

        // Creating the keyboard layout
        ObjectNode replyMarkup = mapper.createObjectNode();
        ArrayNode keyboardArray = mapper.createArrayNode();

        for (int i = 0; i < replyOptions.size(); i++) {
            ArrayNode row = mapper.createArrayNode();

            ObjectNode button = mapper.createObjectNode();
            button.put("text", replyOptions.get(i));
            String encodedCallback = createCallbackJson(replyType, replyOptions.get(i), chatId);
            button.put("callback_data", encodedCallback);
            row.add(button);
            if (i + 1 != replyOptions.size()) {
                ObjectNode button2 = mapper.createObjectNode();
                button2.put("text", replyOptions.get(i + 1));
                String encodedCallback2 = createCallbackJson(replyType, replyOptions.get(i + 1), chatId);
                button2.put("callback_data", encodedCallback2);
                row.add(button2);
                i += 1;
            }
            keyboardArray.add(row);
        }

        replyMarkup.set("inline_keyboard", keyboardArray);

        messageJson.set("reply_markup", replyMarkup);

        String payload;
        try {
            payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return client
                .post()
                .uri("/sendMessage")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> System.err.println("Telegram API Error: " + error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> sendMessage(Long chatId, String message, JsonNode options, String replyType) {
        // Root JSON object
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", message);

        // Creating the keyboard layout
        ObjectNode replyMarkup = mapper.createObjectNode();
        ArrayNode keyboardArray = mapper.createArrayNode();

        var optionsArray = options.get("options");

        for (var pair : optionsArray) {
            ArrayNode row = mapper.createArrayNode();

            ObjectNode button = mapper.createObjectNode();
            button.put("text", pair.get(0).asText());
            String encodedCallback = createCallbackJson(replyType, pair.get(1).asText(), chatId);
            button.put("callback_data", encodedCallback);
            row.add(button);
            keyboardArray.add(row);
        }
        replyMarkup.set("inline_keyboard", keyboardArray);

        messageJson.set("reply_markup", replyMarkup);

        String payload;
        try {
            payload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(messageJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return client
                .post()
                .uri("/sendMessage")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> System.err.println("Telegram API Error: " + error.getMessage()))
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
                .uri("/sendMessage")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> answerCallback(String callbackQueryId, String message, boolean showAlert) {
        ObjectNode callbackJson = mapper.createObjectNode();
        callbackJson.put("callback_query_id", callbackQueryId);
        callbackJson.put("text", message);
        callbackJson.put("show_alert", showAlert);

        String payload;
        try {
            payload = mapper.writeValueAsString(callbackJson);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return client
                .post()
                .uri("/answerCallbackQuery")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                .map(ResponseEntity::ok);
    }

    private String createCallbackJson(String action, String option, Long chatId) {
       return action + ":" + option + ":" + chatId.toString();
    }
}
