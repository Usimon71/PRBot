package ru.udaltsov.data_access.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import ru.udaltsov.data_access.repositories.WebhookRepository;
import ru.udaltsov.models.repositories.IWebhookRepository;

@Configuration
public class PostgresWebhookRepositoryConfig {

    @Bean
    public IWebhookRepository getWebhookRepository(DatabaseClient databaseClient) {
        return new WebhookRepository(databaseClient);
    }
}
