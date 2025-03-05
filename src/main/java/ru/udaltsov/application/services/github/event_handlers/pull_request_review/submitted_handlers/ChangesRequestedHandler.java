package ru.udaltsov.application.services.github.event_handlers.pull_request_review.submitted_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandleResult;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;

import java.util.logging.Handler;

public class ChangesRequestedHandler implements EventHandler {
    @Override
    public Mono<EventHandleResult> handleEvent(JsonNode payload, Long chatId) {
        var pullRequest = payload.get("pull_request");
        var repository = payload.get("repository");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String prTitle = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("title").asText("Untitled"));
        String prNumber = "\\#" + pullRequest.get("number").asText("0");

        var review = payload.get("review");
        String reviewUrl = review.get("html_url").asText("Untitled");

        var reviewer = review.get("user");
        String reviewerLogin = EventMessageFormatter.escapeMarkdownV2("@" + reviewer.get("login").asText());
        String reviewerUrl = reviewer.get("html_url").asText();

        String message = String.format(
                "❌ *Changes requested on* [%s %s%s](%s)\nby [%s](%s)\n",
                repoName, prTitle, prNumber, reviewUrl, reviewerLogin, reviewerUrl
        );

        return Mono.just(new EventHandleResult.Success(message));
    }
}
