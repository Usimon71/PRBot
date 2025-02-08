package ru.udaltsov.application;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebhookConfig {
    public void Setup(){
        String personalAccessToken = System.getenv("GH_PAT");

        // Repository details
        String owner = "Usimon71";  // Replace with the repository owner
        String repo = "draft_project";       // Replace with the repository name

        // Webhook URL where GitHub will send events
        String targetUrl = " https://smee.io/SzNYux7hqbHRxhZ";

        // GitHub API endpoint for creating webhooks
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/hooks";

        // Webhook configuration as JSON
        String webhookConfigJson = """
        {
            "name": "web",
            "active": true,
            "events": ["pull_request"],
            "config": {
                "url": "%s",
                "content_type": "json",
                "insecure_ssl": "0"
            }
        }
        """.formatted(targetUrl);

        // Create HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // Create HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(webhookConfigJson))
                .header("Authorization", "Bearer " + personalAccessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .build();

        try {
            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response details
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
