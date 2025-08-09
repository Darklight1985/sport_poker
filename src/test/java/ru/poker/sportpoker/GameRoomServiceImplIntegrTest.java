package ru.poker.sportpoker;

import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomShortView;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.PlayerInfo;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomPlayerRepository;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUsersServiceImpl;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.TestUtils;
import ru.poker.sportpoker.utils.TokenUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

    @Autowired
    private TestEntityManager entityManager;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final RoomMapper roomMapper = Mappers.getMapper(RoomMapper.class);

    private GameRoomServiceImpl gameRoomService;

    private TokenUtils tokenUtils;

    @BeforeEach
    void setUp() {
        gameRoomService = new GameRoomServiceImpl(gameRoomRepository, keycloakUserService,
                activityUsersServiceImpl, gameRoomPlayerRepository, userMapper, roomMapper);
    }


    private static final String SECRET_KEY = "my-super-secret-key-which-is-32bytes";

    private static final UUID ROOM_ID = UUID.fromString("327d8a5d-7408-4f8d-99aa-b2fcdc39587d");
    private static final UUID ROOM_ID2 = UUID.fromString("327d8a5d-7408-4f8d-99aa-b2fcdc39587e");
    private static final UUID CREATOR_ID = UUID.randomUUID();
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.fromString("227d8a5d-7408-4f8d-99aa-b2fcdc39587d");
    private static final UUID USER_ID2 = UUID.fromString("227d8a5d-7408-4f8d-99aa-b2fcdc39587c");

    private final GameRoom gameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID);
    private final GameRoom emptyGameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID);
    private final PlayerInfo creatorInfo = TestUtils.getUserInfo(CREATOR_ID);
    private final PlayerInfo playerInfo = TestUtils.getUserInfo(PLAYER_ID);
    private final GameRoomPlayer gameRoomPlayer = TestUtils.getGameRoomPlayer(PLAYER_ID, gameRoom);

    private ArgumentCaptor<GameRoomPlayer> gameRoomPlayerArgumentCaptor = ArgumentCaptor.forClass(GameRoomPlayer.class);


    @Nested
    @DisplayName("При обновлении данных о комнате:")
    class UpdateRoomTest {


        @Test
        @Sql(scripts = "classpath:repository/gameRoom/manyRooms.sql")
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
    @Sql(scripts = "classpath:repository/gameRoom/manyRooms.sql")
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

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_METHOD)
        @Sql(scripts = "classpath:repository/gameRoom/manyRooms.sql")
        @DisplayName("При запросе о готовности к игре:")
        class ReadyToGameTest {

            @Test
            @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
            public void testReadyToGame2() {
                when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
                assertThrows(NotFoundException.class, () -> gameRoomService.readyToGame(ROOM_ID));
            }

            @Test
            @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
            public void testReadyToGame3() {
                when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID2));
                GameRoom gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                var result = gameRoomService.readyToGame(ROOM_ID2);
                gameRoomRepository.flush();
                gameRoom = gameRoomRepository.findGameRoomWithPlayers(ROOM_ID2).get();
                assertEquals(true, result);
                assertEquals(1, gameRoom.getVersion());
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_METHOD)
        @DisplayName("При запросе покинуть комнату:")
        class LeftRoomTest {
            @Test
            @DisplayName(" если пользователь есть в комнате, то запрос будет успешен.")
            public void testLeftRoom() {
                when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(PLAYER_ID));
                when(gameRoomPlayerRepository.findByPlayersId(PLAYER_ID)).thenReturn(Optional.of(gameRoomPlayer));
                gameRoomService.leftRoom();

                verify(gameRoomPlayerRepository).delete(gameRoomPlayerArgumentCaptor.capture());
                GameRoomPlayer gameRoomPlayer1 = gameRoomPlayerArgumentCaptor.getValue();
                assertNull(gameRoomPlayer1.getGameRoom());
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_METHOD)
        @DisplayName("При запросе данных о комнате:")
        class KickFromRoomTest {
            @Test
            @DisplayName(" если пользователь есть в комнате, то запрос будет успешен.")
            public void testKickFromRoom() {
                when(gameRoomPlayerRepository.findByPlayersId(PLAYER_ID)).thenReturn(Optional.of(gameRoomPlayer));
                gameRoomService.kickFromRoom(PLAYER_ID);

                verify(gameRoomPlayerRepository).delete(gameRoomPlayerArgumentCaptor.capture());
                GameRoomPlayer gameRoomPlayer1 = gameRoomPlayerArgumentCaptor.getValue();
                assertNull(gameRoomPlayer1.getGameRoom());
            }
        }
    }
}
