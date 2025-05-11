package ru.poker.sportpoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import ru.poker.sportpoker.config.SecurityConfig;

@SpringBootApplication
//@Import(SecurityConfig.class)
public class SportPokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportPokerApplication.class, args);
    }

}
