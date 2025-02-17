package ru.udaltsov.data_access.repositories;

import liquibase.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.IWebhookRepository;

import java.sql.SQLException;
import java.util.UUID;

@Repository
public class WebhookRepository implements IWebhookRepository {

    private final DatabaseClient _databaseClient;

    @Autowired
    public WebhookRepository(DatabaseClient databaseClient) {
        _databaseClient = databaseClient;
    }

    @Override
    public Mono<Long> addWebhook(Webhook webhook) {
        String sql = "INSERT INTO webhooks" +
                " VALUES (:id, :webhook)";

        return _databaseClient
                .sql(sql)
                .bind("id", webhook.id())
                .bind("webhook", webhook.webhook())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                    Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Long> deleteWebhook(Webhook webhook) {
        String sql = "DELETE FROM webhooks" +
                " WHERE id = :id AND webhook = :webhook";

        return _databaseClient
                .sql(sql)
                .bind("id", webhook.id())
                .bind("webhook", webhook.webhook())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Flux<Webhook> findAllById(UUID id) {
        String sql = "SELECT * FROM webhooks " +
                "WHERE id = :id";

        return _databaseClient
                .sql(sql)
                .bind("id", id)
                .map(row -> new Webhook(
                        row.get("id", UUID.class),
                        row.get("webhook", String.class)
                ))
                .all()
                .onErrorResume(SQLException.class, e ->
                Mono.error(new DatabaseException("Database error occurred", e)));
    }
}
