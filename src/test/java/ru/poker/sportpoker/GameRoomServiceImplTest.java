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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.poker.sportpoker.utils.TokenUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

@ExtendWith(MockitoExtension.class)
public class GameRoomServiceImplTest {

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

    private TokenUtils tokenUtils;

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

            Set<GameRoomPlayer> players = gameRoom.getGameRoomPlayers();
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

    //TODO написать тест на TokenUtils
    class GetLinkToRoomTest {

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
            String userId = UUID.randomUUID().toString();

            when(keycloakUserService.getCurrentUser()).thenReturn(userId);
            when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.of(gameRoom));

            ResponseEntity<?> response = gameRoomService.joinRoomByToken(token);

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }

        @Test
        @DisplayName(" если пользователь авторизован а комната не существует то ловим исключение.")
        public void testJoinRoom2() {
            String userId = UUID.randomUUID().toString();

            when(keycloakUserService.getCurrentUser()).thenReturn(userId);
            assertThrows(NotFoundException.class, () -> gameRoomService.joinRoomByToken(token));
        }

        @Test
        @DisplayName(" если ссылка рабочая и комната существует, то пользователь присоединяется к комнате.")
        public void testJoinRoom3() {
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.of(gameRoom));

            ResponseEntity<?> response = gameRoomService.joinRoomByToken(token);

            verify(gameRoomRepository).save(gameRoom);
            Set<GameRoomPlayer> players = gameRoom.getGameRoomPlayers();
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
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(PLAYER_ID));
            when(gameRoomRepository.findGameRoomWithPlayers(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            var result = gameRoomService.readyToGame(ROOM_ID);
            assertEquals(ROOM_ID, gameRoom.getId());
            assertEquals(StatusGame.PLAY, gameRoom.getStatus());
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
