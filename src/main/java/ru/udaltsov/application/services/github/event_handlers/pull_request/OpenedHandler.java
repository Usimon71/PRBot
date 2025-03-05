package ru.udaltsov.application.services.github.event_handlers.pull_request;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandleResult;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;

public class OpenedHandler implements EventHandler {
    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        var pullRequest = payload.get("pull_request");
        var user = pullRequest.get("user");
        var repository = payload.get("repository");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String prTitle = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("title").asText("Untitled"));
        String prNumber = "\\#" + pullRequest.get("number").asText("0"); // Escape #
        String prBody = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("body").asText("No description provided."));
        String userLogin = "@" + EventMessageFormatter.escapeMarkdownV2(user.get("login").asText("Unknown User")); // Plain text, no link
        String prUrl = pullRequest.get("html_url").asText("");
        String userUrl = user.get("html_url").asText("");

        String message = String.format(
                "🔀 *New pull request* [%s %s%s](%s)\nby [%s](%s)\n\n%s",
                repoName, prTitle, prNumber, prUrl, userLogin, userUrl, prBody
        );

        return Mono.just(new EventHandleResult.Success(message));
    }
}
