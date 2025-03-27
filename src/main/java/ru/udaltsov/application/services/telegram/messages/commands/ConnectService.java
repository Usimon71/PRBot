package ru.udaltsov.application.services.telegram.messages.commands;

import org.springframework.http.ResponseEntity;
import ru.udaltsov.application.services.VaultService;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ConnectService{

    private final MessageSender messageSender;
    private final VaultService vaultService;

    @Autowired
    public ConnectService(
            MessageSender messageSender,
            VaultService vaultService) {
        this.messageSender = messageSender;
        this.vaultService = vaultService;
    }

    public Mono<ResponseEntity<String>> ProvideAuthorizeLink(Long chatId) {
        String CLIENT_ID = EventMessageFormatter.escapeMarkdownV2(vaultService.getSecret("CLIENT_ID"));
        String REDIRECT_URL = EventMessageFormatter.escapeMarkdownV2(URLEncoder.encode(System.getenv("REDIRECT_AUTH_URL"), StandardCharsets.UTF_8));
        String link = EventMessageFormatter.escapeMarkdownV2("https://github.com/login/oauth/authorize?client_id=")
                + CLIENT_ID
                + EventMessageFormatter.escapeMarkdownV2("&scope=repo,admin:repo_hook&redirect_uri=")
                + REDIRECT_URL
                + EventMessageFormatter.escapeMarkdownV2("&state=" + chatId);

        var message = EventMessageFormatter.escapeMarkdownV2("Please follow the link to authorize the bot:\n") + link;

        return messageSender.sendMessage(chatId, message);
    }
}