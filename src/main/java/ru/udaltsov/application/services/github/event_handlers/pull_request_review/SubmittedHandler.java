package ru.udaltsov.application.services.github.event_handlers.pull_request_review;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.github.event_handlers.EventHandler;
import ru.udaltsov.application.services.github.event_handlers.EventMessageFormatter;
import ru.udaltsov.application.services.github.event_handlers.pull_request_review.submitted_handlers.SubmittedEventFactory;

public class SubmittedHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        String state = payload.get("review").get("state").asText();

        return new SubmittedEventFactory().getHandler(state).handleEvent(payload, chatId);

        var pullRequest = payload.get("pull_request");
        var user = pullRequest.get("user");
        var repository = payload.get("repository");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String prTitle = EventMessageFormatter.escapeMarkdownV2(pullRequest.get("title").asText("Untitled"));
        String prNumber = "\\#" + pullRequest.get("number").asText("0");

        var review = payload.get("review");
        String reviewUrl = review.get("html_url").asText("Untitled");

        var reviewer = review.get("user");
        String reviewerLogin = reviewer.get("login").asText();
        String reviewerUrl = reviewer.get("html_url").asText();



    }
}
