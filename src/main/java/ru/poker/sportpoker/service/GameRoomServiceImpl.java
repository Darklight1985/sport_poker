package ru.poker.sportpoker.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.repository.GameRoomRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor

//TODO везде добавить валидацию что это комната того кем она создана
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;

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

        gameRoomRepository.deleteById(id);
    }


    @Override
    public String getLinkToRoom(UUID id) {
        GameRoom gameRoomOld = gameRoomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString()));
        return "http://" + address + ":" + port + "/room/join/" + Jwts.builder()
                .claim("roomId", id)
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
