package ru.udaltsov.data_access.repositories;

import liquibase.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.udaltsov.models.Owner;

import java.sql.SQLException;

@Repository
public class OwnerRepositoryImpl implements ru.udaltsov.models.repositories.OwnerRepository {

    private final DatabaseClient _databaseClient;

    @Autowired
    public OwnerRepositoryImpl(DatabaseClient databaseClient) {
        _databaseClient = databaseClient;
    }


    @Override
    public Mono<Long> addOwner(Owner owner) {
        String sql = "INSERT INTO owners (chatid, owner) " +
                "VALUES (:chatid, :owner)";

        return _databaseClient
                .sql(sql)
                .bind("chatid", owner.chatId())
                .bind("owner", owner.owner())
                .fetch()
                .rowsUpdated()
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Owner> getOwnerById(Long chatId) {
        String sql = "SELECT * FROM owners " +
                "WHERE chatid = :chatid";

        return _databaseClient
                .sql(sql)
                .bind("chatid", chatId)
                .map(row -> new Owner(
                        row.get("chatid", Long.class),
                        row.get("owner", String.class)
                ))
                .one()
                .switchIfEmpty(Mono.empty())
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }

    @Override
    public Mono<Owner> getOwnerByOwnerName(String ownerName) {
        String sql = "SELECT * FROM owners " +
                "WHERE owner = :owner";

        return _databaseClient
                .sql(sql)
                .bind("owner", ownerName)
                .map(row -> new Owner(
                        row.get("chatid", Long.class),
                        row.get("owner", String.class)
                ))
                .one()
                .switchIfEmpty(Mono.empty())
                .onErrorResume(SQLException.class, e ->
                        Mono.error(new DatabaseException("Database error occurred", e)));
    }
}
