package ru.udaltsov.data_access.repositories;

import liquibase.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Integration;
import ru.udaltsov.models.repositories.IIntegrationRepository;

import java.sql.SQLException;

@Repository
public class IntegrationRepository implements IIntegrationRepository {

    private final DatabaseClient _databaseClient;

    @Autowired
    public IntegrationRepository(DatabaseClient databaseClient) {
        _databaseClient = databaseClient;
    }

    @Override
    public Flux<Integration> FindAllIntegrationsById(Long chatId) {
        String sql = "SELECT * FROM integrations" +
                " WHERE chatid = :chatId";

        return _databaseClient
                .sql(sql)
                .bind("chatid", chatId)
                .map(row -> new Integration(
                        row.get("chatid", Long.class),
                        row.get("name", String.class)
                ))
                .all()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Long> DeleteIntegration(Integration integration) {
        String sql = "DELETE FROM integrations " +
                "WHERE chatid = :chatid AND name = :name";

        return _databaseClient
                .sql(sql)
                .bind("chatid", integration.chatId())
                .bind("name", integration.repoName())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Long> AddIntegration(Integration integration) {
        String sql = "INSERT INTO integrations (chatid, name) " +
                "VALUES (:chatid, :name)";

        return _databaseClient
                .sql(sql)
                .bind("chatid", integration.chatId())
                .bind("name", integration.repoName())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }
}
