package ru.udaltsov.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.HMACDigestComparator;
import ru.udaltsov.application.HMACSha256Generator;
import org.springframework.web.bind.annotation.*;
import ru.udaltsov.application.services.VaultService;
import ru.udaltsov.application.services.github.WebhookDeliveryService;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/github-webhook")
public class GitHubWebhookController {

    private final WebhookDeliveryService webhookDeliveryService;
    private final VaultService vaultService;

    @Autowired
    public GitHubWebhookController(
            WebhookDeliveryService webhookDeliveryService,
            VaultService vaultService) {
        this.webhookDeliveryService = webhookDeliveryService;
        this.vaultService = vaultService;
    }
    @PostMapping
    public Mono<ResponseEntity<String>> handleWebhook(@RequestHeader("X-GitHub-Event") String eventType,
                                                      @RequestHeader("x-hub-signature-256") String signature,
                                                      @RequestBody String stringPayload) throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("Webhook received!");

        boolean compareResult = HMACDigestComparator.compare(
                HMACSha256Generator.generate(vaultService.getSecret("WEBHOOK_SECRET"), stringPayload),
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

        return webhookDeliveryService.process(payload, eventType);
    }
}
