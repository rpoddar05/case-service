package com.phep.casesvc.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Starts Postgres container eagerly (static block) so Spring can read props safely.
 * No @Testcontainers needed since we start it ourselves.
 */
public abstract class DbIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("casesvc")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start(); // <-- start BEFORE Spring resolves properties
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Ensure Flyway points to the same DB (optional if you don’t set flyway.* elsewhere)
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);

        // Prefer migrations over auto-DDL during ITs:
        // registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
}
