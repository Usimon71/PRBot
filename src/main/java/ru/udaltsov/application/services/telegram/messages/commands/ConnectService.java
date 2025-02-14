package ru.udaltsov.application.services.telegram.messages.commands;

import org.springframework.http.ResponseEntity;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.TelegramServiceBase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ConnectService extends TelegramServiceBase {

    private final MessageSender messageSender;

    @Autowired
    public ConnectService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public Mono<ResponseEntity<String>> ProvideAuthorizeLink(Long chatId) {
        String CLIENT_ID = System.getenv("CLIENT_ID");
        String REDIRECT_URL = System.getenv("REDIRECT_AUTH_URL");
        String link = "https://github.com/login/oauth/authorize?client_id="
                + CLIENT_ID
                + "&scope=repo,admin:repo_hook&redirect_uri="
                + URLEncoder.encode(REDIRECT_URL, StandardCharsets.UTF_8)
                + "&state=" + chatId;
        var message = "Please follow the link to authorize the bot:\n" + link;

        return messageSender.sendMessage(chatId, message);
    }
}