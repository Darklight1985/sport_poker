package ru.poker.sportpoker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ru.poker.sportpoker.TestcontainersConfiguration.POSTGRESQL_CONTAINER;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Slf4j
class SportPokerApplicationTests {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Test
    void contextLoads() {
        System.out.println(POSTGRESQL_CONTAINER.getContainerIpAddress());
        System.out.println(POSTGRESQL_CONTAINER.getJdbcUrl());
        System.out.println("Hello World");
        System.out.println("Active profile: " + activeProfile);
    }

}
