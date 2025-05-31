package ru.udaltsov.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.udaltsov.application.configs.WebClientConfig;
import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.application.services.telegram.messages.exceptions.UserRequestException;
import ru.udaltsov.models.Owner;
import ru.udaltsov.models.repositories.OwnerRepository;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class OwnerServiceTest {

    @Mock private OwnerRepository ownerRepository;
    @Mock private WebClientConfig webClientConfig;
    @Mock private WebClient mockWebClient;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private OwnerService ownerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // WebClient builder chain mocks
        when(webClientConfig.webClientBuilder()).thenReturn(webClientBuilder);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(eq(HttpHeaders.USER_AGENT), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);

        // WebClient method call chain mocks
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        ownerService = new OwnerService(ownerRepository, webClientConfig);
    }

    @Test
    public void testSaveOwner_NewOwner_Success() {
        Map<String, Object> githubResponse = Map.of("login", "testuser");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.empty());
        when(ownerRepository.addOwner(any(Owner.class))).thenReturn(Mono.just(1L));

        StepVerifier.create(ownerService.saveOwner("123", "token"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testSaveOwner_AlreadyExists() {
        Map<String, Object> githubResponse = Map.of("login", "testuser");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.just(new Owner(123L, "testuser")));
        when(ownerRepository.addOwner(any(Owner.class))).thenReturn(Mono.just(1L));

        StepVerifier.create(ownerService.saveOwner("123", "token"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testSaveOwner_GithubLoginMissing() {
        Map<String, Object> githubResponse = Map.of("id", 9999); // No "login"

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));

        StepVerifier.create(ownerService.saveOwner("123", "token"))
                .expectError(UserRequestException.class)
                .verify();
    }

    @Test
    public void testSaveOwner_AddOwnerFails() {
        Map<String, Object> githubResponse = Map.of("login", "testuser");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.empty());
        when(ownerRepository.addOwner(any(Owner.class))).thenReturn(Mono.just(0L)); // Fails to insert

        StepVerifier.create(ownerService.saveOwner("123", "token"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void testFindOwnerByChatId() {
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.just(new Owner(123L, "alice")));

        StepVerifier.create(ownerService.findOwnerByChatId("123"))
                .expectNext("alice")
                .verifyComplete();
    }

    @Test
    public void testFindChatIdByOwnerName() {
        when(ownerRepository.getOwnerByOwnerName("alice")).thenReturn(Mono.just(new Owner(123L, "alice")));

        StepVerifier.create(ownerService.findChatIdByOwnerName("alice"))
                .expectNext(123L)
                .verifyComplete();
    }
}
