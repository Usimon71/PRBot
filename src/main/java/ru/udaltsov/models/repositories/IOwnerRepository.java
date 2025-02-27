package ru.udaltsov.models.repositories;

import reactor.core.publisher.Mono;
import ru.udaltsov.models.Owner;

public interface IOwnerRepository {

    Mono<Long> addOwner(Owner owner);

    Mono<Owner> getOwnerById(Long chatId);

    Mono<Owner> getOwnerByOwnerName(String ownerName);
}
