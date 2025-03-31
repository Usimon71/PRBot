package ru.udaltsov;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import ru.udaltsov.application.services.OwnerService;
import ru.udaltsov.models.repositories.OwnerRepository;

@ExtendWith(MockitoExtension.class)
public class OwnerServiceTest {
    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private final OwnerService ownerService;


}
