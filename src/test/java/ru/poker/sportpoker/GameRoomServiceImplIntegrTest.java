package ru.poker.sportpoker;

import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomPlayerRepository;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUsersServiceImpl;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.TokenUtils;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("classpath:application-test.properties")
@Sql(scripts = "classpath:repository/gameRoom/manyRooms.sql")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
public class GameRoomServiceImplIntegrTest {

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ActivityUsersServiceImpl activityUsersServiceImpl;

    @Autowired
    private GameRoomPlayerRepository gameRoomPlayerRepository;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final RoomMapper roomMapper = Mappers.getMapper(RoomMapper.class);

    private GameRoomServiceImpl gameRoomService;

    private TokenUtils tokenUtils;

    @BeforeEach
    void setUp() {
        gameRoomService = new GameRoomServiceImpl(gameRoomRepository, keycloakUserService,
                activityUsersServiceImpl, gameRoomPlayerRepository, userMapper, roomMapper);
    }

    private static final UUID ROOM_ID = UUID.fromString("327d8a5d-7408-4f8d-99aa-b2fcdc39587d");
    private static final UUID ROOM_ID2 = UUID.fromString("327d8a5d-7408-4f8d-99aa-b2fcdc39587e");
    private static final UUID USER_ID = UUID.fromString("227d8a5d-7408-4f8d-99aa-b2fcdc39587d");
    private static final UUID USER_ID2 = UUID.fromString("227d8a5d-7408-4f8d-99aa-b2fcdc39587c");

    @Nested
    @DisplayName("При обновлении данных о комнате:")
    class UpdateRoomTest {

        @Test
        @DisplayName("если задать корретный ДТО для обновления то информация обновится без ошибок")
        public void testUpdateGameRoom() {
            UpdateGameRoomDto dto = new UpdateGameRoomDto();
            dto.setId(ROOM_ID);
            dto.setName("Updated Room");
            gameRoomService.updateGameRoom(dto);
            gameRoomRepository.flush();
            GameRoom gameRoomNew = gameRoomRepository.findById(ROOM_ID).get();
            int ver = gameRoomNew.getVersion();
            assertEquals(1, ver);
            assertEquals("Updated Room", gameRoomNew.getName());
        }

    }

    @Nested
    @DisplayName("При запросе на вход в комнату по ссылку:")
    class JoinRoomUserTest {

        private String linkWithToken = null;
        String token = null;

        @BeforeEach
        void init() {
            Field secretKeyField;
            Field address;
            Field port;
            try {
                secretKeyField = TokenUtils.class.getDeclaredField("secretKey");
                address = TokenUtils.class.getDeclaredField("address");
                port = TokenUtils.class.getDeclaredField("port");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            secretKeyField.setAccessible(true);
            address.setAccessible(true);
            port.setAccessible(true);
            try {
                secretKeyField.set(tokenUtils, "this-is-secret-only-for-you-loves");
                address.set(tokenUtils, "localhost");
                port.set(tokenUtils, "8083");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            linkWithToken = TokenUtils.getLinkWithToken(ROOM_ID);
            token = linkWithToken.substring(linkWithToken.lastIndexOf("/") + 1);
        }


        @Test
        @DisplayName(" если ссылка рабочая и комната существует, то пользователь присоединяется к комнате.")
        public void testJoinRoom() {

            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            ResponseEntity<?> response = gameRoomService.joinRoomByToken(token);
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            gameRoomRepository.flush();

            GameRoom gameRoom = gameRoomRepository.findById(ROOM_ID).get();
            assertEquals(1, gameRoom.getVersion());
        }
    }

        @Nested
        @DisplayName("При запросе о готовности к игре:")
        class ReadyToGameTest {

            @Test
            @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
            public void testReadyToGame2() {
                when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
                assertThrows(NotFoundException.class, () -> gameRoomService.readyToGame(UUID.randomUUID()));
            }

            @Test
            @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
            public void testReadyToGame3() {
                when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID2));
                var result = gameRoomService.readyToGame(ROOM_ID2);
                gameRoomRepository.flush();
                GameRoom gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                assertEquals(true, result);
                assertEquals(1, gameRoom.getVersion());
            }
        }

        @Nested
        @DisplayName("При запросе покинуть комнату:")
        class LeftRoomTest {
            @Test
            @DisplayName(" если пользователь есть в комнате, то запрос будет успешен.")
            public void testLeftRoom() {
                GameRoom gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                assertEquals(StatusGame.PREP, gameRoom.getStatus());
                assertEquals(0, gameRoom.getVersion());
                when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID2));
                gameRoomService.leftRoom();
                gameRoomRepository.flush();
                gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                assertEquals(StatusGame.PLAY, gameRoom.getStatus());
                assertEquals(1, gameRoom.getVersion());
            }
        }

        @Nested
        @DisplayName("При запросе данных о комнате:")
        class KickFromRoomTest {
            @Test
            @DisplayName(" если пользователь есть в комнате, то запрос будет успешен.")
            public void testKickFromRoom() {
                GameRoom gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                assertEquals(StatusGame.PREP, gameRoom.getStatus());
                assertEquals(0, gameRoom.getVersion());
                gameRoomService.kickFromRoom(USER_ID2);
                gameRoomRepository.flush();
                gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                assertEquals(StatusGame.PLAY, gameRoom.getStatus());
                assertEquals(1, gameRoom.getVersion());
            }
        }

}
