package ru.udaltsov.application.services.telegram;

import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramWebhookSetup {

    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");

    public ResponseEntity<String> SetupWebhook() {
        String webhookUrl = "https://smee.io/xbNQtRy6EUHzagQN";
        String telegramApiUrl = "https://api.telegram.org/bot" + BOT_TOKEN + "/setWebhook";

        try {
            String payload = "{\"url\": \"" + webhookUrl + "\"}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(telegramApiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return ResponseEntity.ok("Webhook set successfully: " + webhookUrl);
            } else {
                return ResponseEntity.status(response.statusCode()).body("Failed to set webhook: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error setting webhook: " + e.getMessage());
        }
    }
}
