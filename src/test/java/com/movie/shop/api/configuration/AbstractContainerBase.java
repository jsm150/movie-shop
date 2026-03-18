package com.movie.shop.api.configuration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

public class AbstractContainerBase {

    @ServiceConnection
    static final MySQLContainer MY_SQL_CONTAINER;

    static {
        MY_SQL_CONTAINER = new MySQLContainer("mysql:8.0");
        MY_SQL_CONTAINER.start();
    }
}
