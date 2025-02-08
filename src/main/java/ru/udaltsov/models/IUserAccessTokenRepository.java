package ru.udaltsov.models;

import reactor.core.publisher.Mono;

public interface IUserAccessTokenRepository {

    Mono<UserAccessToken> FindById(Long id);

    Mono<Long> DeleteById(Long id);

    Mono<Long> Add(UserAccessToken userAccessToken);
}
