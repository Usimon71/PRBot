package ru.udaltsov.application.services.telegram.messages.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GitHubWebhooksProviderService {

    private final MessageSender _messageSender;

    @Autowired
    public GitHubWebhooksProviderService(
            MessageSender messageSender) {
        _messageSender = messageSender;
    }

    public Mono<ResponseEntity<String>> sendWebhooks(Long chatId) throws IOException {
        JsonNode options = readJsonFile("webhook_options.json");

        return _messageSender.sendMessage(
                chatId,
                "What you'd like to track?",
                options,
                "w"
                );
    }

    private JsonNode readJsonFile(String fileName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream = GitHubWebhooksProviderService.class.getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                throw new IOException("File not found in resources: " + fileName);
            }

        return objectMapper.readTree(inputStream);
    }
}
