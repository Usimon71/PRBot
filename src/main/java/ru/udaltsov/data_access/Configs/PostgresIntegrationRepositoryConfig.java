package ru.udaltsov.data_access.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import ru.udaltsov.data_access.repositories.IntegrationRepository;
import ru.udaltsov.models.repositories.IIntegrationRepository;

@Configuration
public class PostgresIntegrationRepositoryConfig {
    @Bean
    public IIntegrationRepository GetPostgresIntegrationRepository(DatabaseClient databaseClient) {
        return new IntegrationRepository(databaseClient);
    }
}
