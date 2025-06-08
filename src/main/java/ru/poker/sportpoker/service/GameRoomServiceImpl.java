package ru.poker.sportpoker.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.repository.GameRoomRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
//TODO везде добавить валидацию что это комната того кем она создана
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final ActivityUsersService activityUsersService;

    private static final String SECRET_KEY = "my-super-secret-key-which-is-32bytes";

    @Value("${application.current-domain}")
    String address;

    @Value("${server.port}")
    String port;


    @Override
    public void createGameRoom(CreateGameRoomDto dto) {
        String userId = keycloakUserService.getCurrentUser();
        GameRoom gameRoom = new GameRoom();
        gameRoom.setName(dto.getName());
        gameRoom.setCreator(UUID.fromString(userId));
        gameRoomRepository.save(gameRoom);
    }

    @Override
    public GameRoom getGameRoom(UUID id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString()));
    }

    @Override
    @Transactional
    public void updateGameRoom(UpdateGameRoomDto dto) {
        GameRoom gameRoomOld = gameRoomRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException(dto.getId().toString()));
        gameRoomOld.setName(dto.getName());
    }

    @Override
    public void deleteGameRoom(UUID id) {
        GameRoom gameRoomOld = gameRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString()));
        gameRoomOld.clearPlayers();
        gameRoomRepository.delete(gameRoomOld);
    }


    @Override
    public String getLinkToRoom(UUID id) {
        gameRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString()));
        return "http://" + address + ":" + port + "/room/join/" + Jwts.builder()
                .claim("roomId", id)
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Override
    @Transactional
    public ResponseEntity<?> joinRoom(String token) {
        String userId = keycloakUserService.getCurrentUser();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/login?redirect=/join/" + token)
                    .build();
        }

        String roomId;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
            roomId = claims.get("roomId", String.class);
        } catch (JwtException e) {
            return ResponseEntity.badRequest().body("Invalid or expired link");
        }

        //TODO добавить проверку
        GameRoom gameRoomOld = gameRoomRepository.findGameRoomWithPlayers(UUID.fromString(roomId))
                .orElseThrow(() -> new NotFoundException(roomId.toString()));
        gameRoomOld.addPlayer(UUID.fromString(userId));
        activityUsersService.joinRoom(UUID.fromString(roomId), UUID.fromString(userId));
        gameRoomRepository.save(gameRoomOld);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    @Transactional
    public void readyToGame(UUID userId, UUID gameRoomId) {
        boolean readyToGame = activityUsersService.setReadyToGame(gameRoomId, userId);
        if (readyToGame) {
            GameRoom gameRoomOld = gameRoomRepository.findById(gameRoomId)
                    .orElseThrow(() -> new NotFoundException(gameRoomId.toString()));
            gameRoomOld.setStatus(StatusGame.PLAY);
        }
    }
}
