package ru.udaltsov.data_access.repositories;

import liquibase.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Webhook;
import ru.udaltsov.models.repositories.WebhookRepository;

import java.sql.SQLException;
import java.util.UUID;

@Repository
public class WebhookRepositoryImpl implements WebhookRepository {

    private final DatabaseClient _databaseClient;

    @Autowired
    public WebhookRepositoryImpl(DatabaseClient databaseClient) {
        _databaseClient = databaseClient;
    }

    @Override
    public Mono<Long> addWebhook(Webhook webhook) {
        String sql = "INSERT INTO webhooks" +
                " VALUES (:integration_id, :webhook, :webhook_id)";

        return _databaseClient
                .sql(sql)
                .bind("integration_id", webhook.integrationId())
                .bind("webhook", webhook.webhook())
                .bind("webhook_id", webhook.webhookId())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                    Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Long> deleteWebhook(Long webhookId) {
        String sql = "DELETE FROM webhooks" +
                " WHERE webhook_id = :webhook_id";

        return _databaseClient
                .sql(sql)
                .bind("webhook_id", webhookId)
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Flux<Webhook> findAllById(UUID integrationId) {
        String sql = "SELECT * FROM webhooks " +
                "WHERE integration_id = :integration_id";

        return _databaseClient
                .sql(sql)
                .bind("integration_id", integrationId)
                .map(row -> new Webhook(
                        row.get("integration_id", UUID.class),
                        row.get("webhook", String.class),
                        row.get("webhook_id", Long.class)
                ))
                .all()
                .onErrorResume(SQLException.class, e ->
                Mono.error(new DatabaseException("Database error occurred", e)));
    }
}
