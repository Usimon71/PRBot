package ru.udaltsov.application.services.telegram.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.udaltsov.application.configs.WebClientConfig;
import ru.udaltsov.application.services.VaultService;
import ru.udaltsov.models.Integration;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageSender {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final VaultService vaultService;
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    public MessageSender(
            WebClientConfig webClientConfig,
            VaultService vaultService) {
        String baseUrl = "https://api.telegram.org";
        this.client = webClientConfig.webClientBuilder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.vaultService = vaultService;
    }

    // Sending normal message
    public Mono<ResponseEntity<String>> sendMessage(Long chatId, String message) {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("chat_id", chatId);
        payloadMap.put("text", message);
        payloadMap.put("parse_mode", "MarkdownV2");
        payloadMap.put("disable_web_page_preview", true);

        String payload;
        try {
            payload = new ObjectMapper().writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        String botToken = vaultService.getSecret("BOT_TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            logger.error("Bot token is empty. Probable cause: Vault problem.");

            return Mono.error(new RuntimeException("Bot token is mandatory"));
        }
        String uri = "/bot" + botToken + "/sendMessage";

        return client
                .post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> sendMessage(Long chatId, String message, List<String> replyOptions, String replyType) {
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", message);

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

        String botToken = vaultService.getSecret("BOT_TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            return Mono.error(new RuntimeException("Bot token is mandatory"));
        }
        String uri = "/bot" + botToken + "/sendMessage";

        return client
                .post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> sendMessage(
            Long chatId,
            String message,
            JsonNode options,
            String replyType,
            String repoName) {
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", message);

        ObjectNode replyMarkup = mapper.createObjectNode();
        ArrayNode keyboardArray = mapper.createArrayNode();

        var optionsArray = options.get("options");

        for (var pair : optionsArray) {
            ArrayNode row = mapper.createArrayNode();

            ObjectNode button = mapper.createObjectNode();
            button.put("text", pair.get(0).asText());
            String encodedCallback = createCallbackJson(replyType, pair.get(1).asText(), chatId, repoName);
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

        String botToken = vaultService.getSecret("BOT_TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            return Mono.error(new RuntimeException("Bot token is mandatory"));
        }
        String uri = "/bot" + botToken + "/sendMessage";

        return client
                .post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> sendIntegrations(Long chatId, List<Integration> integrations) {
        ObjectNode messageJson = mapper.createObjectNode();
        messageJson.put("chat_id", chatId);
        messageJson.put("text", "Select repositories to delete:");

        ObjectNode replyMarkup = mapper.createObjectNode();
        ArrayNode keyboardArray = mapper.createArrayNode();

        for (Integration integration : integrations) {
            ArrayNode row = mapper.createArrayNode();

            ObjectNode button = mapper.createObjectNode();
            button.put("text", integration.repoName());
            String encodedCallback = createCallbackJson("di", integration.id().toString(), chatId);
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

        String botToken = vaultService.getSecret("BOT_TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            return Mono.error(new RuntimeException("Bot token is mandatory"));
        }
        String uri = "/bot" + botToken + "/sendMessage";

        return client
                .post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<String>> removeKeyboard(Long chatId) {
        ObjectMapper mapper = new ObjectMapper();

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

        String botToken = vaultService.getSecret("BOT_TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            return Mono.error(new RuntimeException("Bot token is mandatory"));
        }
        String uri = "/bot" + botToken + "/sendMessage";

        return client
                .post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
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

        String botToken = vaultService.getSecret("BOT_TOKEN");
        if (botToken == null || botToken.isEmpty()) {
            return Mono.error(new RuntimeException("Bot token is mandatory"));
        }
        String uri = "/bot" + botToken + "/answerCallbackQuery";

        return client
                .post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

    private String createCallbackJson(String action, String option, Long chatId) {
       return action + ":" + option + ":" + chatId.toString();
    }

    private String createCallbackJson(String action, String option, Long chatId, String repoName) {
        return action + ":" + option + ":" + chatId.toString() + ":" + repoName;
    }
}
