package ru.udaltsov.data_access.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import ru.udaltsov.data_access.repositories.OwnerRepository;
import ru.udaltsov.models.repositories.IOwnerRepository;

@Configuration
public class PostgresOwnerRepositoryConfig {

    @Bean
    public IOwnerRepository getOwnerRepository(DatabaseClient databaseClient) {
        return new OwnerRepository(databaseClient);
    }
}
