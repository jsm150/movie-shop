package com.movie.shop.api.configuration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers
public interface IntegrationTestContainers {
    @Container
    @ServiceConnection
    MySQLContainer mysql = new MySQLContainer("mysql:8.0.36")
            .withUsername("root")
            .withPassword("root")
            .withDatabaseName("movie-shop");
}
