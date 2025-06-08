package ru.poker.sportpoker.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.UserInfo;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.repository.GameRoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
@Service
@Data
public class ActivityUsersService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;

    private static final Map<UUID, List<UserInfo>> roomPlayersReadyToGame = new ConcurrentHashMap<>();
    private static final Lock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        List<GameRoom> rooms = gameRoomRepository.findGameRoomByStatusEquals(StatusGame.PREP);
        for (GameRoom room : rooms) {
            List<UserInfo> userInfoList = keycloakUserService.getUsersInfo(room.getPlayers());
            lock.lock();
            try {
                roomPlayersReadyToGame.put(room.getId(), userInfoList);
            } finally {
                lock.unlock();
            }
        }
    }

    public boolean setReadyToGame(UUID roomId, UUID userId) {
        List<UserInfo> userInfoList = roomPlayersReadyToGame.get(roomId);
        if (userInfoList == null) {
            return false;
        }
        lock.lock();
        try {
            for (UserInfo userInfo : userInfoList) {
                if (userInfo.getUserId().equals(userId)) {
                    userInfo.setReady(true);
                }
            }
            for (UserInfo userInfo : userInfoList) {
                if (!userInfo.isReady()) {
                    return false;
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public void joinRoom(UUID roomId, UUID userId) {
        List<UserInfo> userInfoList = roomPlayersReadyToGame.get(roomId);
        if (userInfoList == null) {
            userInfoList = new ArrayList<>();
            lock.lock();
            try {
                roomPlayersReadyToGame.put(roomId, userInfoList);
            } finally {
                lock.unlock();
            }
        }
        UserInfo userInfo = keycloakUserService.getUserInfo(userId);
        lock.lock();
        try {
            userInfoList.add(userInfo);
        } finally {
            lock.unlock();
        }
    }

}
