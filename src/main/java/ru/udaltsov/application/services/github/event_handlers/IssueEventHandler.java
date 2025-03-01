package ru.udaltsov.application.services.github.event_handlers;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public class IssueEventHandler implements EventHandler {
    @Override
    public Mono<String> handleEvent(JsonNode payload, Long chatId) {
        var issue = payload.get("issue");
        var user = issue.get("user");
        var repository = payload.get("repository");

        String repoName = EventMessageFormatter.escapeMarkdownV2(EventMessageFormatter.extractRepoName(repository.get("full_name").asText("Unknown Repository")));
        String issueTitle = EventMessageFormatter.escapeMarkdownV2(issue.get("title").asText("Untitled"));
        String issueNumber = "\\#" + issue.get("number").asText("0"); // Escape #
        String issueBody = EventMessageFormatter.escapeMarkdownV2(issue.get("body").asText("No description provided."));
        String userLogin = "@" + EventMessageFormatter.escapeMarkdownV2(user.get("login").asText("Unknown User"));
        String userUrl = user.get("html_url").asText("");
        String issueUrl = issue.get("html_url").asText(""); // DO NOT escape URL

        // Construct the message for issue event
        String message = String.format(
                "🐛 **New issue** [%s %s%s](%s)\nby [%s](%s)\n\n%s",
                repoName, issueTitle, issueNumber, issueUrl, userLogin, userUrl, issueBody
        );

        return Mono.just(message);
    }
}
