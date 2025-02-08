package ru.udaltsov.data_access;

import liquibase.exception.DatabaseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import ru.udaltsov.models.IUserAccessTokenRepository;
import ru.udaltsov.models.UserAccessToken;

import java.sql.SQLException;

@Repository
public class UserAccessTokenRepository implements IUserAccessTokenRepository {

//    private final Connection _connection;

    private final DatabaseClient _databaseClient;

    private static final Logger logger = LoggerFactory.getLogger(UserAccessTokenRepository.class);

    @Autowired
    public UserAccessTokenRepository(DatabaseClient databaseClient) {
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

//        try {
//            PreparedStatement statement= _connection
//                    .prepareStatement(sql);
//            statement.setLong(1, id);
//            ResultSet resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                return new UserAccessTokenFindResult.Success(new UserAccessToken(
//                        resultSet.getLong("chatid"),
//                        resultSet.getString("access_token")
//                        )
//                );
//            }
//
//            return new UserAccessTokenFindResult.NotFound();
//        } catch (SQLException e) {
//            logger.error("Error saving access_token with chatId: {}, SQL State: {}, Error Code: {}",
//                    id, e.getSQLState(), e.getErrorCode(), e);
//        }
//
//        return new UserAccessTokenFindResult.Failure();
    }

    @Override
    public Mono<Long> DeleteById(Long id) {
        String sql = "DELETE FROM chat " +
                "WHERE chatid = $1";

        return _databaseClient
                .sql(sql)
                .bind(0, id)
                .fetch()
                .rowsUpdated();

//        try {
//            PreparedStatement stmt = _connection
//                    .prepareStatement(sql);
//
//            stmt.setLong(1, id);
//            int rowsDeleted = stmt.executeUpdate();
//
//            if (rowsDeleted > 0) {
//                logger.info("User with ID {} deleted successfully", id);
//
//                return new UserAccessTokenDeleteResult.Success();
//            }
//
//            logger.warn("No user found with ID {} to delete", id);
//
//            return new UserAccessTokenDeleteResult.NotFound();
//        } catch (SQLException e) {
//            logger.error("Error deleting access_token with chatId {}: {}, SQL State: {}, Error Code: {}",
//                    id, e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
//
//            return new UserAccessTokenDeleteResult.Failure();
//        }
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
                .rowsUpdated();
//        try {
//            PreparedStatement statement = _connection.prepareStatement(sql);
//            statement.setLong(1, userAccessToken.id());
//            statement.setString(2, userAccessToken.token());
//
//            statement.executeUpdate();
//
//            return new UserAccessTokenAddResult.Success();
//
//        } catch (SQLException e) {
//            logger.error("Error saving access token with chatId: {}, SQL State: {}, Error Code: {}",
//                    userAccessToken.id(), e.getSQLState(), e.getErrorCode(), e);
//        }
//
//        return new UserAccessTokenAddResult.Failure();
    }
}
