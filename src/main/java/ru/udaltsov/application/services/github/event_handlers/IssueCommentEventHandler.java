package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public class IssueCommentEventHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        var comment = payload.get("comment");
        var issue = payload.get("issue");
        var user = comment.get("user");
        var repository = payload.get("repository");

        // Extract comment details
        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String commentBody = EventMessageFormatter.escapeMarkdownV2(comment.get("body").asText("No comment text provided."));
        String commenterLogin = "@" + EventMessageFormatter.escapeMarkdownV2(user.get("login").asText("Unknown User"));
        String commentUrl = comment.get("html_url").asText("");

        // Extract issue details
        String issueTitle = EventMessageFormatter.escapeMarkdownV2(issue.get("title").asText("Untitled"));
        String issueNumber = "\\#" + issue.get("number").asText("0"); // Escape #
        String issueUrl = issue.get("html_url").asText("");
        String commenterUrl = user.get("html_url").asText("");

        // Construct the message for issue comment event
        String message = String.format(
                "💬 **New comment** on [%s %s%s](%s)\nby [%s](%s)\n\n%s\n\n[View comment](%s)",
                repoName, issueTitle, issueNumber, issueUrl, commenterLogin, commenterUrl, commentBody, commentUrl
        );

        System.out.println("Issue Comment Event Message: " + message);
        return Mono.just(message);
    }
}
