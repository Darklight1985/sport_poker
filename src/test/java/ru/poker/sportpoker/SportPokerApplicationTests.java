package ru.poker.sportpoker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ru.poker.sportpoker.TestcontainersConfiguration.POSTGRESQL_CONTAINER;

@Testcontainers
@SpringBootTest
@Slf4j
class SportPokerApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(POSTGRESQL_CONTAINER.getContainerIpAddress());
        System.out.println(POSTGRESQL_CONTAINER.getJdbcUrl());
        System.out.println("Hello World");
    }

}
