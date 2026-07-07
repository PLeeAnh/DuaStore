package com.duastore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ★ DuaStoreApplication — Entry point, Spring Boot (port 8080)
 * Client: /  |  Admin: /admin
 * DB: SQL Server (application.properties) + DuaStore_Database.sql
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class DuaStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuaStoreApplication.class, args);
    }

}
