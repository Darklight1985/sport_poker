package ru.poker.sportpoker.utils;

import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.UserInfo;
import ru.poker.sportpoker.dto.UserShortInfo;

import java.util.UUID;

public class TestUtils {

    public static GameRoom getGameRoom(UUID roomId) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(roomId);
        return gameRoom;
    }

    public static GameRoom getGameRoom(UUID roomId, UUID creatorId, UUID playerId) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setCreator(creatorId);
        gameRoom.setId(roomId);
        gameRoom.addPlayer(playerId);
        return gameRoom;
    }

    public static UserInfo getUserInfo(UUID userId) {
        return UserInfo.builder()
                .userId(userId)
                .build();
    }
}
