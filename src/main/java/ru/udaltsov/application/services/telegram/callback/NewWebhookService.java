package ru.udaltsov.application.services.telegram.callback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NewWebhookService {

    private final WebClient _githubClient;

    @Autowired
    NewWebhookService(WebClient.Builder webClientBuilder) {
        _githubClient = webClientBuilder.build();
    }
}
