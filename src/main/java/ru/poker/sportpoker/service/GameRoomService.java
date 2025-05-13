package ru.poker.sportpoker.service;

import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;

import java.util.UUID;

public interface GameRoomService {

    public void createGameRoom(CreateGameRoomDto dto);

    public GameRoom getGameRoom(UUID id);

    public void updateGameRoom(UUID id, GameRoom gameRoom);

    public void deleteGameRoom(UUID id);
}
