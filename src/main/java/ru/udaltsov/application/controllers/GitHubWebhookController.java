package ru.udaltsov.application.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.HMACDigestComparator;
import ru.udaltsov.application.HMACSha256Generator;
import org.springframework.web.bind.annotation.*;
import ru.udaltsov.application.services.github.WebhookDeliveryService;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/github-webhook")
public class GitHubWebhookController {

    private final WebhookDeliveryService webhookDeliveryService;

    @Autowired
    public GitHubWebhookController(WebhookDeliveryService webhookDeliveryService) {
        this.webhookDeliveryService = webhookDeliveryService;
    }
    @PostMapping
    public Mono<ResponseEntity<String>> handleWebhook(@RequestHeader("X-GitHub-Event") String eventType,
                                                      @RequestHeader("x-hub-signature-256") String signature,
                                                      @RequestBody String stringPayload) throws NoSuchAlgorithmException, InvalidKeyException {
        // Log incoming payload
        System.out.println("Webhook received!");

        boolean compareResult = HMACDigestComparator.compare(
                HMACSha256Generator.generate(System.getenv("WEBHOOK_SECRET"), stringPayload),
                signature);

        if (!compareResult) {
            System.out.println("Signature verification failed!");

            return Mono.error(new Exception("Signature verification failed!"));
        } else {
            System.out.println("Signature verified!");
        }

        JsonNode payload;
        try {
            payload = new ObjectMapper().readTree(stringPayload);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        // Return a 200 OK response
        return webhookDeliveryService.process(payload, eventType);
    }
}
