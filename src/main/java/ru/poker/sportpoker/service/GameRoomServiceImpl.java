package ru.poker.sportpoker.service;

import io.jsonwebtoken.JwtException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.dto.*;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.mapper.RoomMapper;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.repository.GameRoomPlayerRepository;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.utils.TokenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final ActivityUserService activityUserService;
    private final GameRoomPlayerRepository gameRoomPlayerRepository;
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;

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
        GameRoom gameRoom = activityUserService.getActiveRoom(id);
        if (gameRoom == null) {
            gameRoom = gameRoomRepository.findGameRoomWithPlayers(id)
                    .orElseThrow(() -> new NotFoundException(id.toString()));
        }
        Set<UUID> players = gameRoom.getGameRoomPlayers().stream()
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
        roomMapper.updateGameRoom(gameRoomOld, dto);
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
        return TokenUtils.getLinkWithToken(id);
    }

    @Override
    @Transactional
    public ResponseEntity<?> joinRoomByToken(String token) {
        String roomId;
        try {
            roomId = TokenUtils.getRoomId(token);
        } catch (JwtException e) {
            return ResponseEntity.badRequest().body("Invalid or expired link");
        }

        return joinRoom(UUID.fromString(roomId));
    }

    @Override
    public ResponseEntity<?> joinRoomByPassword(UUID roomId) {
        return joinRoom(roomId);
    }

    private ResponseEntity<?> joinRoom (UUID roomId) {
        String userId = keycloakUserService.getCurrentUser();

        GameRoom gameRoomOld = gameRoomRepository.findGameRoomWithPlayers(roomId)
                .orElseThrow(() -> new NotFoundException(roomId.toString()));
        GameRoomPlayer gameRoomPlayer = new GameRoomPlayer();
        gameRoomPlayer.setPlayersId(UUID.fromString(userId));
        gameRoomPlayer.setGameRoom(gameRoomOld);

        gameRoomRepository.save(gameRoomOld);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }


    @Override
    public boolean readyToGame(UUID gameRoomId) {
        String userId = keycloakUserService.getCurrentUser();

        GameRoom gameRoomOld = gameRoomRepository.findGameRoomWithPlayers(gameRoomId)
                .orElseThrow(() -> new NotFoundException(gameRoomId.toString()));

        GameRoomPlayer gameRoomPlayer = gameRoomOld.getPlayer(UUID.fromString(userId));
        gameRoomPlayer.setReady(true);
        GameRoom gameRoom = gameRoomPlayer.getGameRoom();
        boolean readyToGame = true;
        Set<GameRoomPlayer> players = gameRoom.getGameRoomPlayers();
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
    }

    @Override
    @Transactional
    //TODO необходимо написать условие на удаление, что если комната уже в игре то удалить нельзя или выйти самому
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
