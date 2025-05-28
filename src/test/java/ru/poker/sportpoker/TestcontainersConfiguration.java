package ru.poker.sportpoker;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:13")
                .withDatabaseName("integration-tests-db")
                .withUsername("sa")
                .withPassword("password");
        POSTGRESQL_CONTAINER.start();
    }

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return POSTGRESQL_CONTAINER;
    }

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }
}
