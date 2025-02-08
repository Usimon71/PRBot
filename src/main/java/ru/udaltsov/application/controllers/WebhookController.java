package ru.udaltsov.application.controllers;

import ru.udaltsov.application.HMACDigestComparator;
import ru.udaltsov.application.HMACSha256Generator;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    @PostMapping
    public String handleWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) throws NoSuchAlgorithmException, InvalidKeyException {
        // Log incoming payload
        System.out.println("Webhook received!");

        boolean compareResult = HMACDigestComparator.compare(
                HMACSha256Generator.generate("hello1", payload),
                headers.get("x-hub-signature-256"));

        if (!compareResult) {
            System.out.println("Signature verification failed!");

            return "Signature verification failed!";
        } else {
            System.out.println("Signature verified!");
        }
        System.out.println("Headers: " + headers);
        System.out.println("Payload: " + payload);

        // Return a 200 OK response
        return "Webhook processed successfully!";
    }
}
