package ru.udaltsov.application.services.github.event_handlers.pull_request;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;

public class ClosedHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        var pullRequest = payload.get("pull_request");
        var user = pullRequest.get("user");
        var repository = payload.get("repository");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String prTitle = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("title").asText("Untitled"));
        String prNumber = "\\#" + pullRequest.get("number").asText("0"); // Escape #
        String userLogin = "@" + EventMessageFormatter.escapeMarkdownV2(user.get("login").asText("Unknown User")); // Plain text, no link
        String prUrl = pullRequest.get("html_url").asText("");
        String userUrl = user.get("html_url").asText("");

        boolean isMerged = pullRequest.get("merged").asBoolean(false);

        String message;
        if (isMerged) {
            message = String.format(
                    "🎉 *Pull request merged* [%s %s%s](%s)\nby [%s](%s)\n",
                    repoName, prTitle, prNumber, prUrl, userLogin, userUrl
            );
        } else {
            message = String.format(
                    "❌ *Pull request closed * [%s %s%s](%s)\nby [%s](%s)\n",
                    repoName, prTitle, prNumber, prUrl, userLogin, userUrl
            );
        }

        return Mono.just(message);
    }
}
