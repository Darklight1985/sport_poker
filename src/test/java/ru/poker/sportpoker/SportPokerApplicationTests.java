package ru.poker.sportpoker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.profiles.active=test")
class SportPokerApplicationTests {

 //   @Mock
  //  private GameRoomRepository gameRoomRepository;

    @Test
    void contextLoads() {
    }

}
