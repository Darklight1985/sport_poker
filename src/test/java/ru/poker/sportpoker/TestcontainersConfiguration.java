package ru.poker.sportpoker;

import jakarta.activation.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestcontainersConfiguration {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15.3")
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

//    @Bean
//    @Primary
//    public DataSource dataSource() {
//        // Создаем DataSource с параметрами из контейнера
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl(POSTGRESQL_CONTAINER.getJdbcUrl());
//        dataSource.setUsername(POSTGRESQL_CONTAINER.getUsername());
//        dataSource.setPassword(POSTGRESQL_CONTAINER.getPassword());
//        return (DataSource) dataSource;
//    }
}
