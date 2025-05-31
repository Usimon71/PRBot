package ru.udaltsov.application.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> next.exchange(request)
                        .flatMap(response -> {
                            if (response.statusCode().is5xxServerError()) {
                                return Mono.error(new RuntimeException("5xx error"));
                            }
                            return Mono.just(response);
                        })
                        .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100)))
                        .onErrorResume(throwable -> {
                            log.error("Request failed: {}", throwable.getMessage());

                            return Mono.error(new WebClientException("WebClient Error: " + throwable.getMessage()));
                        })
                );
    }
}
