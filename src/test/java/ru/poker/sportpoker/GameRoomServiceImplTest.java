package ru.poker.sportpoker;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.*;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUsersService;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.TestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GameRoomServiceImplTest {

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ActivityUsersService activityUsersService;

    @Spy
    private UserMapper userMapper;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    private static final String SECRET_KEY = "my-super-secret-key-which-is-32bytes";

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final UUID CREATOR_ID = UUID.randomUUID();
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private final GameRoom gameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID, PLAYER_ID);
    private final UserInfo creatorInfo = TestUtils.getUserInfo(CREATOR_ID);
    private final UserInfo playerInfo = TestUtils.getUserInfo(PLAYER_ID);


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При создании комнаты:")
    class CreateRoomTest {


        @Test
        @DisplayName("""
                если задать дто, то комната сохранится""")
        public void testCreateGameRoom() {
            CreateGameRoomDto dto = new CreateGameRoomDto();
            dto.setName("Test Room");

            String userId = UUID.randomUUID().toString();
            when(keycloakUserService.getCurrentUser()).thenReturn(userId);
            gameRoomService.createGameRoom(dto);

            verify(gameRoomRepository).save(any(GameRoom.class));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class GetRoomTest {


        @Test
        public void testGetGameRoom() {
            UUID roomId = UUID.randomUUID();
            GameRoom gameRoom = new GameRoom();
            gameRoom.setId(roomId);

            when(gameRoomRepository.findGameRoomWithPlayers(roomId)).thenReturn(Optional.of(gameRoom));
            when(roomMapper.getView(gameRoom, null, Set.of())).thenReturn(new GameRoomView());

            GameRoomView view = gameRoomService.getGameRoom(roomId);


            assertNotNull(view);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных обо всех комнатах:")
    class GetRoomsTest {

        @Test
        public void testGetGameRooms() {
            when(gameRoomRepository.findAll()).thenReturn(List.of(gameRoom));
            when(activityUsersService.getActiveRoom(ROOM_ID)).thenReturn(gameRoom);
            when(keycloakUserService.getUserInfo(CREATOR_ID)).thenReturn(creatorInfo);
            when(keycloakUserService.getUsersInfo(Set.of(PLAYER_ID))).thenReturn(Set.of(playerInfo));
            // Add more mocks for other dependencies and assertions as needed

            List<GameRoomView> rooms = gameRoomService.getGameRooms();

            assertEquals(1, rooms.size());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class UpdateRoomTest {

        @Test
        public void testUpdateGameRoom() {
            UpdateGameRoomDto dto = new UpdateGameRoomDto();
            dto.setId(ROOM_ID);
            dto.setName("Updated Room");

            when(gameRoomRepository.findById(dto.getId())).thenReturn(Optional.of(gameRoom));

            gameRoomService.updateGameRoom(dto);

            assertEquals("Updated Room", gameRoom.getName());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class DeleteRoomTest {

        @Test
        public void testDeleteGameRoom() {
            UUID roomId = UUID.randomUUID();
            GameRoom gameRoom = new GameRoom();
            gameRoom.setId(roomId);

            when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(gameRoom));

            gameRoomService.deleteGameRoom(roomId);

            verify(gameRoomRepository).delete(eq(gameRoom));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class GetLinkToRoomTest {
        @Test
        public void testGetLinkToRoom() {
            ReflectionTestUtils.setField(gameRoomService, "address", "localhost");
            ReflectionTestUtils.setField(gameRoomService, "port", "8080");
            String link = "http://localhost:8080/room/join/";

            when(gameRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(gameRoom));

            String result = gameRoomService.getLinkToRoom(ROOM_ID);
            assertTrue(result.startsWith(link));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class JoinRoomUserTest {
        @Test
        public void testJoinRoomUserNull() {
            String token = "valid-token";

            when(keycloakUserService.getCurrentUser()).thenReturn(null);
            ResponseEntity<?> response = gameRoomService.joinRoom(token);

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }


        @Test
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
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class ReadyToGameTest {
        @Test
        public void testReadyToGame() {
            UUID userId = UUID.randomUUID();

            when(keycloakUserService.getCurrentUser()).thenReturn(userId.toString());
            when(activityUsersService.setReadyToGame(ROOM_ID, userId)).thenReturn(false);
            //  when(gameRoomRepository.findGameRoomWithPlayers(roomId)).thenReturn(Optional.of(new GameRoom()));

            boolean result = gameRoomService.readyToGame(ROOM_ID);

            assertFalse(result);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class LeftRoomTest {
        @Test
        public void testLeftRoom() {
            String userId = UUID.randomUUID().toString();

            gameRoom.setId(ROOM_ID);
            when(gameRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            when(keycloakUserService.getCurrentUser()).thenReturn(userId);

            gameRoomService.leftRoom(ROOM_ID);

            verify(gameRoomRepository).save(eq(gameRoom));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе данных о комнате:")
    class KickFromRoomTest {
        @Test
        public void testKickFromRoom() {
            UUID roomId = UUID.randomUUID();
            UUID playerId = UUID.randomUUID();

            GameRoom gameRoom = new GameRoom();
            gameRoom.setId(roomId);
            when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(gameRoom));

            gameRoomService.kickFromRoom(roomId, playerId);

            verify(gameRoomRepository).save(eq(gameRoom));
        }
    }
}
