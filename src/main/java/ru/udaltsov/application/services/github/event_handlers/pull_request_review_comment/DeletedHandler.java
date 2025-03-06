package ru.udaltsov.application.services.github.event_handlers.pull_request_review_comment;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandleResult;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;

public class DeletedHandler implements EventHandler {
    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        var pullRequest = payload.get("pull_request");
        var repository = payload.get("repository");
        var comment = payload.get("comment");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String prTitle = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("title").asText("Untitled"));
        String prNumber = "\\#" + pullRequest.get("number").asText("0");

        var commenter = comment.get("user");
        String commenterLogin = EventMessageFormatter.escapeMarkdownV2("@" + commenter.get("login").asText());
        String commenterUrl = commenter.get("html_url").asText();

        String message = String.format(
                "\uD83D\uDDD1\uD83D\uDCAC *Comment on review deleted* [%s %s%s]\nby [%s](%s)",
                repoName, prTitle, prNumber, commenterLogin, commenterUrl
        );

        return Mono.just(new EventHandleResult.Success(message));
    }
}
