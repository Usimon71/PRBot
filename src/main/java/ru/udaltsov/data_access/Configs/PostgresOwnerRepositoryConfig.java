package ru.udaltsov.data_access.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import ru.udaltsov.models.repositories.OwnerRepository;

@Configuration
public class PostgresOwnerRepositoryConfig {

    @Bean
    public OwnerRepository getOwnerRepository(DatabaseClient databaseClient) {
        return new ru.udaltsov.data_access.repositories.OwnerRepository(databaseClient);
    }
}
