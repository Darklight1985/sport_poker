package ru.poker.sportpoker;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.dto.*;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomPlayerRepository;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUsersServiceImpl;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.TestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameRoomServiceImplTest {

    @Mock
    private Lock lock;

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ActivityUsersServiceImpl activityUsersServiceImpl;

    @Mock
    private GameRoomPlayerRepository gameRoomPlayerRepository;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final RoomMapper roomMapper = Mappers.getMapper(RoomMapper.class);

    private GameRoomServiceImpl gameRoomService;

    @BeforeEach
    void setUp() {
        gameRoomService = new GameRoomServiceImpl(gameRoomRepository, keycloakUserService,
                activityUsersServiceImpl, gameRoomPlayerRepository, userMapper, roomMapper);
    }


    private static final String SECRET_KEY = "my-super-secret-key-which-is-32bytes";

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final UUID CREATOR_ID = UUID.randomUUID();
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private final GameRoom gameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID);
    private final GameRoom emptyGameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID);
    private final UserInfo creatorInfo = TestUtils.getUserInfo(CREATOR_ID);
    private final UserInfo playerInfo = TestUtils.getUserInfo(PLAYER_ID);
    private final GameRoomPlayer gameRoomPlayer = TestUtils.getGameRoomPlayer(PLAYER_ID, gameRoom);

    private ArgumentCaptor<GameRoomPlayer> gameRoomPlayerArgumentCaptor = ArgumentCaptor.forClass(GameRoomPlayer.class);


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При создании комнаты:")
    class CreateRoomTest {

        private ArgumentCaptor<GameRoom> gameRoomCaptor = ArgumentCaptor.forClass(GameRoom.class);

        @Test
        @DisplayName("""
                если задать дто, то комната сохранится""")
        public void testCreateGameRoom() {
            CreateGameRoomDto dto = new CreateGameRoomDto();
            dto.setName("Test Room");
            dto.setGameTime(1);
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            when(gameRoomRepository.save(gameRoomCaptor.capture())).thenReturn(emptyGameRoom);
            gameRoomService.createGameRoom(dto);

            GameRoom gameRoomAfterSave = gameRoomCaptor.getValue();
            assertEquals(dto.getName(), gameRoomAfterSave.getName());
            assertEquals(USER_ID, gameRoomAfterSave.getCreator());
            assertEquals(StatusGame.PREP, gameRoomAfterSave.getStatus());
            assertNotNull(gameRoomAfterSave.getGameTime());

            Set<GameRoomPlayer> players = gameRoom.getPlayers();
            assertEquals(1, players.size());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class GetRoomTest {


        @Test
        @DisplayName(" если запросить существующий идентификатор то получим информацию о комнате")
        public void testGetGameRoom() {
            when(gameRoomRepository.findGameRoomWithPlayers(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            GameRoomView view = gameRoomService.getGameRoom(ROOM_ID);
            assertNotNull(view);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных обо всех комнатах:")
    class GetRoomsTest {

        @Test
        @DisplayName(" получаем данные по всем комнатам")
        public void testGetGameRooms() {
            when(gameRoomRepository.findAll()).thenReturn(List.of(gameRoom));
            when(activityUsersServiceImpl.getActiveRoom(ROOM_ID)).thenReturn(gameRoom);
            when(keycloakUserService.getUserInfo(CREATOR_ID)).thenReturn(creatorInfo);
            when(keycloakUserService.getUsersInfo(Set.of(PLAYER_ID))).thenReturn(Set.of(playerInfo));
            List<GameRoomView> rooms = gameRoomService.getGameRooms();
            assertEquals(1, rooms.size());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При обновлении данных о комнате:")
    class UpdateRoomTest {

        @Test
        @DisplayName("если задать корретный ДТО для обновления то информация обновится без ошибок")
        public void testUpdateGameRoom() {
            UpdateGameRoomDto dto = new UpdateGameRoomDto();
            dto.setId(ROOM_ID);
            dto.setName("Updated Room");
            when(gameRoomRepository.findById(dto.getId())).thenReturn(Optional.of(gameRoom));
            gameRoomService.updateGameRoom(dto);
            assertEquals("Updated Room", gameRoom.getName());
        }

        @Test
        public void testDeleteGameRoom2() {
            UpdateGameRoomDto dto = new UpdateGameRoomDto();
            dto.setId(UUID.randomUUID());
            dto.setName("Updated Room");
            assertThrows(NotFoundException.class, () -> gameRoomService.updateGameRoom(dto));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе на удаление комнаты:")
    class DeleteRoomTest {

        @Test
        public void testDeleteGameRoom() {
            when(gameRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            gameRoomService.deleteGameRoom(ROOM_ID);
            verify(gameRoomRepository).delete(eq(gameRoom));
        }

        @Test
        public void testDeleteGameRoom2() {
            assertThrows(NotFoundException.class, () -> gameRoomService.deleteGameRoom(UUID.randomUUID()));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе ссылки для присоединения к комнате:")
    class GetLinkToRoomTest {
        @Test
        @DisplayName(" если комната существует, то получаем в ответ ссылку.")
        public void testGetLinkToRoom() {
            ReflectionTestUtils.setField(gameRoomService, "address", "localhost");
            ReflectionTestUtils.setField(gameRoomService, "port", "8080");
            String link = "http://localhost:8080/room/join/";

            when(gameRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(gameRoom));

            String result = gameRoomService.getLinkToRoom(ROOM_ID);
            assertTrue(result.startsWith(link));
        }

        @Test
        @DisplayName(" если комната не существует, то ловим исключение.")
        public void testDeleteGameRoom2() {
            assertThrows(NotFoundException.class, () -> gameRoomService.getLinkToRoom(UUID.randomUUID()));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе на вход в комнату по ссылку:")
    class JoinRoomUserTest {

        @Test
        @DisplayName(" если пользователь не авторизовался то перенаправляем его на логин.")
        public void testJoinRoomUserNull() {
            String token = "valid-token";
            when(keycloakUserService.getCurrentUser()).thenReturn(null);
            ResponseEntity<?> response = gameRoomService.joinRoom(token);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }


        @Test
        @DisplayName(" если ссылка рабочая и комната существует, то пользователь присоединяется к комнате.")
        public void testJoinRoom() {
            String token = Jwts.builder()
                    .claim("roomId", ROOM_ID)
                    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .compact();
            String userId = UUID.randomUUID().toString();

            when(keycloakUserService.getCurrentUser()).thenReturn(userId);
            when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.of(gameRoom));

            ResponseEntity<?> response = gameRoomService.joinRoom(token);

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }

        @Test
        @DisplayName(" если пользователь авторизован а комната не существует то ловим исключение.")
        public void testJoinRoom2() {
            String token = Jwts.builder()
                    .claim("roomId", ROOM_ID)
                    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .compact();
            String userId = UUID.randomUUID().toString();

            when(keycloakUserService.getCurrentUser()).thenReturn(userId);
            assertThrows(NotFoundException.class, () -> gameRoomService.joinRoom(token));
        }

        @Test
        @DisplayName(" если ссылка рабочая и комната существует, то пользователь присоединяется к комнате.")
        public void testJoinRoom3() {
            String token = Jwts.builder()
                    .claim("roomId", ROOM_ID)
                    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .compact();

            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.of(gameRoom));

            ResponseEntity<?> response = gameRoomService.joinRoom(token);

            verify(gameRoomRepository).save(gameRoom);
            Set<GameRoomPlayer> players = gameRoom.getPlayers();
            assertEquals(2, players.size());
            GameRoomPlayer gameRoomPlayer = players.stream().findFirst().get();
            assertFalse(gameRoom.getPlayer(USER_ID).isReady());
            assertNotNull(gameRoom.getPlayer(USER_ID));
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе о готовности к игре:")
    class ReadyToGameTest {

        @Test
        @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
        public void testReadyToGame2() {
            UUID roomId = UUID.randomUUID();
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            assertThrows(NotFoundException.class, () -> gameRoomService.readyToGame(roomId));
        }

        @Test
        @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
        public void testReadyToGame3() {
            ReflectionTestUtils.setField(gameRoomService, "lock", lock);
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(PLAYER_ID));
            when(gameRoomRepository.findGameRoomWithPlayers(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            var result = gameRoomService.readyToGame(ROOM_ID);
            assertEquals(ROOM_ID, gameRoom.getId());
            assertEquals(StatusGame.PLAY, gameRoom.getStatus());
            verify(lock, times(1)).lock();
            verify(lock, times(1)).unlock();
            verify(activityUsersServiceImpl).activeRoom(gameRoom);
            assertTrue(result);
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
    @DisplayName("При выполнении запроса выкинуть пользователя из комнаты:")
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
