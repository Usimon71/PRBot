package ru.udaltsov.data_access.repositories;

import liquibase.exception.DatabaseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import ru.udaltsov.models.UserAccessToken;

import java.sql.SQLException;

@Repository
public class UserAccessTokenRepositoryImpl implements ru.udaltsov.models.repositories.UserAccessTokenRepository {

    private final DatabaseClient _databaseClient;

    private static final Logger logger = LoggerFactory.getLogger(UserAccessTokenRepositoryImpl.class);

    @Autowired
    public UserAccessTokenRepositoryImpl(DatabaseClient databaseClient) {
        _databaseClient = databaseClient;
    }


    @Override
    public Mono<UserAccessToken> FindById(Long id) {
        String sql = "SELECT * FROM chat" +
                " WHERE chatid = $1";
        return _databaseClient
                .sql(sql)
                .bind(0, id)
                .map(row -> new UserAccessToken(
                        row.get("chatid", Long.class),
                        row.get("access_token", String.class)
                ))
                .one()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Long> DeleteById(Long id) {
        String sql = "DELETE FROM chat " +
                "WHERE chatid = $1";

        return _databaseClient
                .sql(sql)
                .bind(0, id)
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Long> Add(UserAccessToken userAccessToken) {
        String sql = "INSERT INTO chat (chatid, access_token) " +
                "VALUES ($1, $2)";

        return _databaseClient
                .sql(sql)
                .bind(0, userAccessToken.id())
                .bind(1, userAccessToken.token())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }
}
