package ru.udaltsov.data_access.Configs;

import ru.udaltsov.data_access.repositories.UserAccessTokenRepository;
import ru.udaltsov.models.repositories.IUserAccessTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class PostgresUserAccessTokenRepositoryConfig {
    @Bean
    public static IUserAccessTokenRepository GetPostgresUserAccessTokenRepository(DatabaseClient databaseClient) {
        return new UserAccessTokenRepository(databaseClient);
    }
}
