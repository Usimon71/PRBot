package ru.udaltsov.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.udaltsov.application.services.EncryptionService;
import ru.udaltsov.application.services.telegram.messages.UserService;
import ru.udaltsov.models.UserAccessToken;
import ru.udaltsov.models.repositories.UserAccessTokenRepository;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;


public class UserServiceTest {

    @Mock
    private UserAccessTokenRepository userAccessTokenRepository;

    @Mock
    private EncryptionService encryptionService;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userAccessTokenRepository, encryptionService);
    }


    @Test
    public void findUserToken_shouldReturnUserAccessToken() {
        var token = new UserAccessToken(1L, "token");
        when(userAccessTokenRepository.FindById(any(Long.class))).thenReturn(Mono.just(token));
        when(encryptionService.decrypt(any(String.class))).thenReturn("token");

        StepVerifier.create(userService.findUserToken("1"))
                .expectNext("token")
                .verifyComplete();
    }

    @Test
    public void saveUserToken_shouldReturnTrue() {
        when(encryptionService.encrypt(any(String.class))).thenReturn("token");
        when(userAccessTokenRepository.Add(any(UserAccessToken.class))).thenReturn(Mono.just(1L));

        StepVerifier.create(userService.saveUserToken("1", "token"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void saveUserToken_shouldReturnFalse() {
        when(encryptionService.encrypt(any(String.class))).thenReturn("token");
        when(userAccessTokenRepository.Add(any(UserAccessToken.class))).thenReturn(Mono.just(0L));

        StepVerifier.create(userService.saveUserToken("1", "token"))
                .expectNext(false)
                .verifyComplete();
    }
}
