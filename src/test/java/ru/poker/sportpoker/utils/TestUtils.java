package ru.poker.sportpoker.utils;

import org.keycloak.representations.idm.UserRepresentation;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.dto.PlayerInfo;

import java.util.UUID;

public class TestUtils {

    public static GameRoom getGameRoom(UUID roomId) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(roomId);
        return gameRoom;
    }

    public static GameRoom getGameRoom(UUID roomId, UUID creatorId) {
        GameRoom gameRoom = new GameRoom();
        gameRoom.setCreator(creatorId);
        gameRoom.setId(roomId);
        return gameRoom;
    }

    public static PlayerInfo getUserInfo(UUID userId) {
        return PlayerInfo.builder()
                .userId(userId)
                .build();
    }

    public static GameRoomPlayer getGameRoomPlayer(UUID playerId, GameRoom gameRoom) {
        GameRoomPlayer gameRoomPlayer = new GameRoomPlayer();
        gameRoomPlayer.setPlayersId(playerId);
        gameRoomPlayer.setGameRoom(gameRoom);
        return gameRoomPlayer;
    }

    public static UserRepresentation getUserRepresentation(UUID userId, String username, String firstName, String lastName, String email) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(String.valueOf(userId));
        userRepresentation.setUsername(username);
        userRepresentation.setFirstName(firstName);
        userRepresentation.setLastName(lastName);
        userRepresentation.setEmail(email);
        return userRepresentation;
    }
}
