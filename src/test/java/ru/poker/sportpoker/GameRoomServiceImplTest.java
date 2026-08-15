package ru.poker.sportpoker;

import com.google.common.collect.Sets;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomShortView;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.PlayerInfo;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomPlayerRepository;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUserService;
import ru.poker.sportpoker.service.DeckService;
import ru.poker.sportpoker.service.ExerciseMapper;
import ru.poker.sportpoker.service.GameRoomSseService;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.service.MinioFileService;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.utils.TestUtils;
import ru.poker.sportpoker.utils.TokenUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    private ActivityUserService activityUserService;

    @Mock
    private GameRoomPlayerRepository gameRoomPlayerRepository;

    @Mock
    private DeckService deckService;

    @Mock
    private ExerciseMapper exerciseMapper;

    @Mock
    private MinioFileService minioFileService;

    @Mock
    private GameRoomSseService sseService;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final RoomMapper roomMapper = Mappers.getMapper(RoomMapper.class);

    private GameRoomServiceImpl gameRoomService;

    @BeforeEach
    void setUp() {
        gameRoomService = new GameRoomServiceImpl(gameRoomRepository, keycloakUserService,
                activityUserService, gameRoomPlayerRepository, userMapper, roomMapper,
                deckService, exerciseMapper, minioFileService, sseService);
    }


    private static final String SECRET_KEY = "my-super-secret-key-which-is-32bytes";

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final UUID CREATOR_ID = UUID.randomUUID();
    private static final UUID PLAYER_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private final GameRoom gameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID);
    private final GameRoom emptyGameRoom = TestUtils.getGameRoom(ROOM_ID, CREATOR_ID);
    private final PlayerInfo creatorInfo = TestUtils.getUserInfo(CREATOR_ID);
    private final PlayerInfo playerInfo = TestUtils.getUserInfo(PLAYER_ID);
    private final GameRoomPlayer gameRoomPlayer = TestUtils.getGameRoomPlayer(PLAYER_ID, gameRoom);
    private final Set<Exercises> exercises = Set.of(Exercises.BURPEE, Exercises.DEADLIFT, Exercises.HANDSTAND, Exercises.BOX_JUMP);

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
            dto.setExercises(exercises);

            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            when(gameRoomRepository.save(gameRoomCaptor.capture())).thenReturn(emptyGameRoom);
            gameRoomService.createGameRoom(dto);

            GameRoom gameRoomAfterSave = gameRoomCaptor.getValue();
            assertEquals(dto.getName(), gameRoomAfterSave.getName());
            assertEquals(USER_ID, gameRoomAfterSave.getCreator());
            assertEquals(StatusGame.PREP, gameRoomAfterSave.getStatus());
            assertNotNull(gameRoomAfterSave.getGameTime());
            assertTrue(Sets.difference(exercises, gameRoomAfterSave.getExercises()).isEmpty());

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
            when(gameRoomRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl(List.of(gameRoom)));
            Page<GameRoomShortView> rooms =
                    gameRoomService.getGameRooms(PageRequest.of(0, 10), null, null);
            assertEquals(1, rooms.getTotalElements());
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
            dto.setExercises(exercises);

            when(gameRoomRepository.findById(dto.getId())).thenReturn(Optional.of(gameRoom));
            gameRoomService.updateGameRoom(dto);
            assertEquals("Updated Room", gameRoom.getName());
            assertTrue(Sets.difference(exercises, gameRoom.getExercises()).isEmpty());
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
        @DisplayName(" если комната не существует, то ловим исключение.")
        public void testGetLinkToRoomNotFound() {
            when(gameRoomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> gameRoomService.getLinkToRoom(ROOM_ID));
        }

        @Test
        @DisplayName(" если комната существует, то ссылка генерируется.")
        public void testGetLinkToRoomExists() {
            when(gameRoomRepository.findById(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            String link = gameRoomService.getLinkToRoom(ROOM_ID);
            assertNotNull(link);
            assertTrue(link.contains("/room/join/"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе на вход в комнату по ссылку:")
    class JoinRoomByTokenTest {

        @Test
        @DisplayName(" если токен невалидный, то возвращаем Bad Request.")
        public void testJoinRoomInvalidToken() {
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            ResponseEntity<?> response = gameRoomService.joinRoomByToken("invalid-token");
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName(" если комната не существует, то ловим исключение.")
        public void testJoinRoomRoomNotFound() {
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> gameRoomService.joinRoomByToken("some-token"));
        }

        @Test
        @DisplayName(" если комната существует и в стадии PREP, то пользователь присоединяется.")
        public void testJoinRoomSuccess() {
            String userId = UUID.randomUUID().toString();

            when(keycloakUserService.getCurrentUser()).thenReturn(userId);
            when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.of(gameRoom));

            ResponseEntity<?> response = gameRoomService.joinRoomByToken("some-token");

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            verify(gameRoomRepository).save(gameRoom);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При запросе о готовности к игре:")
    class ReadyToGameTest {

        @Test
        @DisplayName(" если пользователь не участник комнаты, то ловим исключение.")
        public void testReadyToGameNotFound() {
            UUID roomId = UUID.randomUUID();
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(USER_ID));
            assertThrows(NotFoundException.class, () -> gameRoomService.readyToGame(roomId));
        }

        @Test
        @DisplayName(" если пользователь участник комнаты, то запрос будет успешным.")
        public void testReadyToGameSuccess() {
            when(keycloakUserService.getCurrentUser()).thenReturn(String.valueOf(PLAYER_ID));
            when(gameRoomRepository.findGameRoomWithPlayers(ROOM_ID)).thenReturn(Optional.of(gameRoom));
            when(sseService.addSubscriber(ROOM_ID, PLAYER_ID)).thenReturn(new SseEmitter());

            SseEmitter result = gameRoomService.readyToGame(ROOM_ID);
            
            assertNotNull(result);
            assertEquals(StatusGame.PLAY, gameRoom.getStatus());
            verify(activityUserService).activeRoom(gameRoom);
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
