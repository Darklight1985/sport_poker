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
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.dto.*;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomPlayerRepository;
import ru.poker.sportpoker.repository.GameRoomRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//TODO везде добавить валидацию что это комната того кем она создана
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final ActivityUserService activityUserService;
    private final GameRoomPlayerRepository gameRoomPlayerRepository;
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;
    private final Lock lock = new ReentrantLock();

    private static final String SECRET_KEY = "my-super-secret-key-which-is-32bytes";

    @Value("${application.current-domain}")
    String address;

    @Value("${server.port}")
    String port;


    @Override
    @Transactional
    public void createGameRoom(CreateGameRoomDto dto) {
        String userId = keycloakUserService.getCurrentUser();
        GameRoom gameRoom = roomMapper.toGameRoom(dto, UUID.fromString(userId));

        gameRoom = gameRoomRepository.save(gameRoom);

        GameRoomPlayer gameRoomPlayer = new GameRoomPlayer();
        gameRoomPlayer.setPlayersId(UUID.fromString(userId));
        gameRoomPlayer.setGameRoom(gameRoom);
        gameRoomPlayerRepository.save(gameRoomPlayer);
    }

    @Override
    public GameRoomView getGameRoom(UUID id) {
        //TODO Переделать, вначале проверить статус, если она не на финише и не подготовке то тащим из активных комнат
        GameRoom gameRoom = activityUserService.getActiveRoom(id);
        if (gameRoom == null) {
            gameRoom = gameRoomRepository.findGameRoomWithPlayers(id)
                    .orElseThrow(() -> new NotFoundException(id.toString()));
        }
        Set<UUID> players = gameRoom.getPlayers().stream()
                .map(GameRoomPlayer::getPlayersId)
                .collect(Collectors.toSet());

        UUID creatorId = gameRoom.getCreator();
        UserShortInfo creatorInfo = userMapper.getUserShortInfo(keycloakUserService.getUserInfo(creatorId));
        Set<UserShortInfo> playersInfo = userMapper.getUserShortInfoList(keycloakUserService.getUsersInfo(players));
        return roomMapper.getView(gameRoom, creatorInfo, playersInfo);
    }

    @Override
    @Transactional
    public List<GameRoomView> getGameRooms() {
        List<GameRoom> gameRooms = (List<GameRoom>) gameRoomRepository.findAll();
        List<GameRoomView> gameRoomViews = new ArrayList<>();
        gameRooms.forEach(gameRoom -> gameRoomViews.add(getGameRoom(gameRoom.getId())));
        return gameRoomViews;
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
                    .header("Location", "/login?redirect=/room/join/" + token)
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
        GameRoomPlayer gameRoomPlayer = new GameRoomPlayer();
        gameRoomPlayer.setPlayersId(UUID.fromString(userId));
        gameRoomPlayer.setGameRoom(gameRoomOld);


        //gameRoomPlayerRepository.save(gameRoomPlayer);
        gameRoomRepository.save(gameRoomOld);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    @Transactional
    //TODO здесь нужна блокировка чтобы никто не вышел за это время
    public boolean readyToGame(UUID gameRoomId) {
        String userId = keycloakUserService.getCurrentUser();
        lock.lock();
        try {
            GameRoom gameRoomOld = gameRoomRepository.findGameRoomWithPlayers(gameRoomId)
                    .orElseThrow(() -> new NotFoundException(gameRoomId.toString()));

            GameRoomPlayer gameRoomPlayer = gameRoomOld.getPlayer(UUID.fromString(userId));
            gameRoomPlayer.setReady(true);
            GameRoom gameRoom = gameRoomPlayer.getGameRoom();
            boolean readyToGame = true;
            Set<GameRoomPlayer> players = gameRoom.getPlayers();
            for (GameRoomPlayer player : players) {
                if (!player.isReady()) {
                    readyToGame = false;
                }
            }
            if (readyToGame) {
                gameRoomOld.setStatus(StatusGame.PLAY);
                activityUserService.activeRoom(gameRoomOld);
            }
            return readyToGame;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional
    public void leftRoom() {
        String userId = keycloakUserService.getCurrentUser();
        removePlayer(UUID.fromString(userId));
    }

    @Override
    @Transactional
    public void kickFromRoom(UUID playerId) {
        removePlayer(playerId);
    }

    private void removePlayer(UUID playerId) {
        GameRoomPlayer gameRoomPlayer = gameRoomPlayerRepository.findByPlayersId(playerId)
                .orElseThrow(() -> new NotFoundException());

        gameRoomPlayer.deleteGameRoom();
        gameRoomPlayerRepository.delete(gameRoomPlayer);
    }
}
