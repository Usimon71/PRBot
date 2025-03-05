package ru.udaltsov.application.services.github.event_handlers.pull_request_review_comment;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandleResult;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;

public class CreatedHandler implements EventHandler {
    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        var pullRequest = payload.get("pull_request");
        var repository = payload.get("repository");
        var comment = payload.get("comment");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String prTitle = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("title").asText("Untitled"));
        String prNumber = "\\#" + pullRequest.get("number").asText("0");

        var commenter = comment.get("user");
        String commenterLogin = EventMessageFormatter.escapeMarkdownV2("@" + comment.get("login").asText());
        String commenterUrl = commenter.get("html_url").asText();

        String commentBody = EventMessageFormatter.escapeMarkdownV2(comment.get("body").asText());
        String commentUrl = comment.get("html_url").asText();

        String message = String.format(
                "💬 **New comment on review** [%s %s%s]\nby [%s](%s)\n\n%s\n\n[View comment](%s)",
                repoName, prTitle, prNumber, commenterLogin, commenterUrl, commentBody, commentUrl
        );

        return Mono.just(new EventHandleResult.Success(message));
    }
}
