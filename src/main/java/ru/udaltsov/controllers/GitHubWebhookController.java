package ru.udaltsov.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(GitHubWebhookController.class);

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
        logger.info("GitHub webhook received. Event type: {}", eventType);

        boolean compareResult = HMACDigestComparator.compare(
                HMACSha256Generator.generate(vaultService.getSecret("WEBHOOK_SECRET"), stringPayload),
                signature);

        if (!compareResult) {
            logger.warn("HMAC SHA256 check failed. Event type: {}", eventType);

            return Mono.error(new Exception("Signature verification failed!"));
        } else {
            logger.info("HMAC SHA256 check successful.");
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
