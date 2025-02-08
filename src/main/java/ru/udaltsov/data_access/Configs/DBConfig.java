package ru.udaltsov.data_access.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DBConfig {
    private static final String URL = "jdbc:postgresql://localhost:6432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    @Bean
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
