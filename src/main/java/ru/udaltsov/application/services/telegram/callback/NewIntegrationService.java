package ru.udaltsov.application.services.telegram.callback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.udaltsov.application.services.telegram.messages.MessageSender;
import ru.udaltsov.models.Integration;
import ru.udaltsov.models.repositories.IIntegrationRepository;

@Service
public class NewIntegrationService {

    private final IIntegrationRepository _integrationRepository;

    public NewIntegrationService(
            IIntegrationRepository integrationRepository) {
        _integrationRepository = integrationRepository;
    }

    public Mono<ResponseEntity<String>> saveIntegration(Long chatId, String name) {
        return _integrationRepository.FindIntegrationsByIdAndName(chatId, name)
                .flatMap(found -> {
                    if (found) {
                        return Mono
                                .just(ResponseEntity.ok("Integration already exists"));
                    }
                    return _integrationRepository.AddIntegration(new Integration(chatId, name))
                            .flatMap(result -> {
                                if (result == 0) {
                                    return Mono
                                            .just(ResponseEntity.internalServerError()
                                                    .body("Failed to save integration"));
                                }

                                return Mono
                                        .just(ResponseEntity.ok("Successfully saved integration"));
                            });
                });
    }
}
