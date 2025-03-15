package ru.udaltsov.application.services.telegram.callback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Integration;
import ru.udaltsov.models.repositories.IntegrationRepository;

import java.util.UUID;

@Service
public class NewIntegrationService {

    private final IntegrationRepository _integrationRepository;

    public NewIntegrationService(
            IntegrationRepository integrationRepository) {
        _integrationRepository = integrationRepository;
    }

    public Mono<ResponseEntity<String>> saveIntegration(Long chatId, String name) {
        return _integrationRepository.FindIntegrationByIdAndName(chatId, name)
                .flatMap(integration ->
                    Mono.just(ResponseEntity.ok("Integration already exists"))
                )
                .switchIfEmpty( _integrationRepository.AddIntegration(new Integration(UUID.randomUUID(), chatId, name))
                        .flatMap(result -> {
                            if (result == 0) {
                                return Mono
                                        .just(ResponseEntity.internalServerError()
                                                .body("Failed to save integration"));
                            }

                            return Mono
                                    .just(ResponseEntity.ok("Successfully saved integration"));
                        }));
    }
}
