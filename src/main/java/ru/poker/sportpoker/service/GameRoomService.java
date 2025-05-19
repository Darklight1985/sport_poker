package ru.poker.sportpoker.service;

import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;

import java.util.UUID;

public interface GameRoomService {

    public void createGameRoom(CreateGameRoomDto dto);

    public GameRoom getGameRoom(UUID id);

    public void updateGameRoom(UpdateGameRoomDto dto);

    public void deleteGameRoom(UUID id);

    String getLinkToRoom(UUID id);
}
