package ru.udaltsov.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.application.services.telegram.messages.exceptions.UserRequestException;
import ru.udaltsov.models.Owner;
import ru.udaltsov.models.repositories.OwnerRepository;

import java.util.Map;

import static org.mockito.Mockito.*;

public class OwnerServiceTest {

    private OwnerRepository ownerRepository;

    @Mock
    private WebClient mockWebClient;

    private OwnerService ownerService;


    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        ownerRepository = mock(OwnerRepository.class);
        mockWebClient = mock(WebClient.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(eq(HttpHeaders.USER_AGENT), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mockWebClient);

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        ownerService = new OwnerService(ownerRepository, builder);
    }

    @Test
    public void testSaveOwner_NewOwner_Success() {
        String chatId = "123";
        String token = "valid_token";

        Map<String, Object> githubResponse = Map.of("login", "testuser");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.empty());
        when(ownerRepository.addOwner(new Owner(123L, "testuser"))).thenReturn(Mono.just(1L));

        StepVerifier.create(ownerService.saveOwner(chatId, token))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testSaveOwner_AlreadyExists() {
        String chatId = "123";
        String token = "valid_token";

        Map<String, Object> githubResponse = Map.of("login", "testuser");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));
        when(ownerRepository.addOwner(any(Owner.class))).thenReturn(Mono.just(1L));
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.just(new Owner(123L, "testuser")));

        StepVerifier.create(ownerService.saveOwner(chatId, token))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testSaveOwner_GithubLoginMissing() {
        String chatId = "123";
        String token = "invalid_token";

        Map<String, Object> githubResponse = Map.of("id", 9999); // No "login"

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));

        StepVerifier.create(ownerService.saveOwner(chatId, token))
                .expectError(UserRequestException.class)
                .verify();
    }

    @Test
    public void testSaveOwner_AddOwnerFails() {
        String chatId = "123";
        String token = "valid_token";

        Map<String, Object> githubResponse = Map.of("login", "testuser");

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(githubResponse));
        when(ownerRepository.getOwnerById(123L)).thenReturn(Mono.empty());
        when(ownerRepository.addOwner(new Owner(123L, "testuser"))).thenReturn(Mono.just(0L)); // Fail

        StepVerifier.create(ownerService.saveOwner(chatId, token))
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
