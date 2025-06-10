package ru.poker.sportpoker.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.UserInfo;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.event.GameEndEvent;
import ru.poker.sportpoker.repository.GameRoomRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
@Service
@Data
@Slf4j
public class ActivityUsersServiceImpl implements ActivityUserService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final ApplicationEventPublisher eventPublisher;

    //TODO по этой мапе можно проводить валидацию что если комната уже здесь то все выйти из игры уже не можешь
    private static final Map<UUID, GameRoom> activeRoom = new ConcurrentHashMap<>();
    private static final Lock lock = new ReentrantLock();

    public GameRoom getActiveRoom(UUID roomId) {
        return activeRoom.get(roomId);
    }

    public void activeRoom(GameRoom gameRoom) {
        gameRoom.letsPlay(eventPublisher);
        activeRoom.put(gameRoom.getId(), gameRoom);
    }

    @EventListener
    public void endGame(GameEndEvent event) {
        GameRoom gameRoom = activeRoom.get(event.getRoomId());
        if (gameRoom != null) {
            log.debug("Игра в комнате {} окончена", gameRoom.getName());
            gameRoom.setStatus(StatusGame.END);
            activeRoom.remove(event.getRoomId());
            gameRoomRepository.save(gameRoom);
        }
    }
}
