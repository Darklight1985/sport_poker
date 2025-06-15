package ru.poker.sportpoker;

import com.fasterxml.jackson.jakarta.rs.json.annotation.JSONP;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUsersService;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.service.KeycloakUserService;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameRoomServiceImplTest {

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ActivityUsersService activityUsersService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoomMapper roomMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateGameRoom() {
        CreateGameRoomDto dto = new CreateGameRoomDto();
        dto.setName("Test Room");

        String userId = UUID.randomUUID().toString();
        when(keycloakUserService.getCurrentUser()).thenReturn(userId);

        gameRoomService.createGameRoom(dto);

        verify(gameRoomRepository).save(any(GameRoom.class));
    }

    @Test
    //TODO доправить тест
    public void testGetGameRoom() {
        UUID roomId = UUID.randomUUID();
        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(roomId);

        when(gameRoomRepository.findGameRoomWithPlayers(roomId)).thenReturn(Optional.of(gameRoom));
        when(roomMapper.getView(gameRoom, null, Set.of())).thenReturn(new GameRoomView());

        GameRoomView view = gameRoomService.getGameRoom(roomId);


        assertNotNull(view);
    }

    @Test
    public void testGetGameRooms() {
        when(gameRoomRepository.findAll()).thenReturn(List.of(new GameRoom(), new GameRoom()));
        // Add more mocks for other dependencies and assertions as needed

        List<GameRoomView> rooms = gameRoomService.getGameRooms();

        assertEquals(2, rooms.size());
    }

    @Test
    public void testUpdateGameRoom() {
        UpdateGameRoomDto dto = new UpdateGameRoomDto();
        dto.setId(UUID.randomUUID());
        dto.setName("Updated Room");

        GameRoom gameRoom = new GameRoom();
        when(gameRoomRepository.findById(dto.getId())).thenReturn(Optional.of(gameRoom));

        gameRoomService.updateGameRoom(dto);

        verify(gameRoomRepository).save(eq(gameRoom));
    }

    @Test
    public void testDeleteGameRoom() {
        UUID roomId = UUID.randomUUID();
        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(roomId);

        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(gameRoom));

        gameRoomService.deleteGameRoom(roomId);

        verify(gameRoomRepository).delete(eq(gameRoom));
    }

    @Test
    public void testGetLinkToRoom() {
        UUID roomId = UUID.randomUUID();
        String link = "http://example.com";

        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(new GameRoom()));

        String result = gameRoomService.getLinkToRoom(roomId);

        assertEquals(link, result);
    }

    @Test
    public void testJoinRoom() {
        String token = "valid-token";
        String userId = UUID.randomUUID().toString();

        when(keycloakUserService.getCurrentUser()).thenReturn(userId);
        when(gameRoomRepository.findGameRoomWithPlayers(any())).thenReturn(Optional.of(new GameRoom()));

        ResponseEntity<?> response = gameRoomService.joinRoom(token);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void testReadyToGame() {
        UUID roomId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();

        when(keycloakUserService.getCurrentUser()).thenReturn(userId);
        when(gameRoomRepository.findGameRoomWithPlayers(roomId)).thenReturn(Optional.of(new GameRoom()));

        boolean result = gameRoomService.readyToGame(roomId);

        assertTrue(result);
    }

    @Test
    public void testLeftRoom() {
        UUID roomId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();

        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(roomId);
        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(gameRoom));

        gameRoomService.leftRoom(roomId);

        verify(gameRoomRepository).save(eq(gameRoom));
    }

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
