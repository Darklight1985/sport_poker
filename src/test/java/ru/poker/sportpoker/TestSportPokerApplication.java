package ru.poker.sportpoker;

import org.springframework.boot.SpringApplication;

public class TestSportPokerApplication {

    public static void main(String[] args) {
        SpringApplication.from(SportPokerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
