package ru.udaltsov.models.repositories;

import reactor.core.publisher.Mono;
import ru.udaltsov.models.UserAccessToken;

public interface IUserAccessTokenRepository {

    Mono<UserAccessToken> FindById(Long id);

    Mono<Long> DeleteById(Long id);

    Mono<Long> Add(UserAccessToken userAccessToken);
}
