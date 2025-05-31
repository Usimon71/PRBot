package ru.udaltsov.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.udaltsov.application.configs.WebClientConfig;
import ru.udaltsov.application.services.VaultService;
import ru.udaltsov.application.services.telegram.messages.TokenService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TokenServiceTest {

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient tokenClient;
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private WebClient.RequestBodySpec requestBodySpec;
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;
    @Mock private VaultService vaultService;
    @Mock private WebClientConfig webClientConfig;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(webClientConfig.webClientBuilder()).thenReturn(webClientBuilder);
        when(webClientBuilder.baseUrl(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(any(), any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(tokenClient);

        when(vaultService.getSecret("CLIENT_ID")).thenReturn("CLIENT_ID");
        when(vaultService.getSecret("SECRET")).thenReturn("SECRET");

        tokenService = new TokenService(webClientConfig, vaultService);
    }

    @Test
    void requestToken_shouldReturnToken() {
        when(tokenClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("access_token", "token")));

        StepVerifier.create(tokenService.requestToken("code"))
                .expectNext("token")
                .verifyComplete();
    }
}
