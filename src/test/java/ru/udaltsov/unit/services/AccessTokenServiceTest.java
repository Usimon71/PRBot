package ru.udaltsov.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.application.services.telegram.messages.TokenService;
import ru.udaltsov.application.services.telegram.messages.UserService;

import static org.mockito.Mockito.mock;

public class AccessTokenServiceTest {

    @Mock
    private MessageSender messageSender;

    @Mock
    private UserService userService;


    @BeforeEach
    public void setUp() {
        messageSender = mock(MessageSender.class);

    }
}
