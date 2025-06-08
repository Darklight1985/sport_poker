package ru.poker.sportpoker;

import com.fasterxml.jackson.jakarta.rs.json.annotation.JSONP;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.ActivityUsersService;
import ru.poker.sportpoker.service.GameRoomServiceImpl;
import ru.poker.sportpoker.service.KeycloakUserService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameRoomServiceImplTest {

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ActivityUsersService activityUsersService;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gameRoomService = new GameRoomServiceImpl(gameRoomRepository, keycloakUserService, activityUsersService);
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
    public void testGetGameRoom_existingId() {
        UUID id = UUID.randomUUID();
        GameRoom gameRoom = new GameRoom();
        when(gameRoomRepository.findById(id)).thenReturn(Optional.of(gameRoom));

        GameRoom result = gameRoomService.getGameRoom(id);

        assertEquals(gameRoom, result);
    }

    @Test
    public void testGetGameRoom_nonExistingId() {
        UUID id = UUID.randomUUID();
        when(gameRoomRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> gameRoomService.getGameRoom(id));
    }

    @Test
    public void testUpdateGameRoom() {
        UpdateGameRoomDto dto = new UpdateGameRoomDto();
        dto.setId(UUID.randomUUID());
        dto.setName("Updated Room");

        GameRoom gameRoomOld = new GameRoom();
        when(gameRoomRepository.findById(dto.getId())).thenReturn(Optional.of(gameRoomOld));

        gameRoomService.updateGameRoom(dto);

        assertEquals("Updated Room", gameRoomOld.getName());
    }

    @Test
    public void testDeleteGameRoom_Success() {
        UUID id = UUID.randomUUID();
        GameRoom gameRoom = new GameRoom();
        when(gameRoomRepository.findById(id)).thenReturn(Optional.of(gameRoom));

        gameRoomService.deleteGameRoom(id);

        verify(gameRoomRepository).findById(id);
        verify(gameRoomRepository).delete(gameRoom);
    }

    @Test
    public void testDeleteGameRoom_NotFoundException() {
        UUID id = UUID.randomUUID();
        when(gameRoomRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> gameRoomService.deleteGameRoom(id));

        assertEquals(id.toString(), exception.getMessage());
    }

//    @Test
//    public void testJoinRoom_validToken() {
//        String token = "valid.token";
//       // Claims claims = new DefaultClaims();
//        UUID roomId = UUID.randomUUID();
//      //  claims.put("roomId", roomId);
//
//        GameRoom gameRoomOld = new GameRoom();
//        when(gameRoomRepository.findById(roomId)).thenReturn(Optional.of(gameRoomOld));
//        when(keycloakUserService.getCurrentUser()).thenReturn("user123");
//
//        ResponseEntity<?> response = gameRoomService.joinRoom(token);
//
//        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
//    }

    @Test
    public void testJoinRoom_invalidToken() {
        String token = "invalid.token";

        ResponseEntity<?> response = gameRoomService.joinRoom(token);

        assertEquals("Invalid or expired link", Objects.requireNonNull(response.getBody()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
