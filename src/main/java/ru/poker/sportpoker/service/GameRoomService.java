package ru.poker.sportpoker.service;

import org.springframework.http.ResponseEntity;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;

import java.util.UUID;

public interface GameRoomService {

    /**
     * Создание игровой комнаты
     * @param dto
     */
    void createGameRoom(CreateGameRoomDto dto);

    GameRoom getGameRoom(UUID id);

    void updateGameRoom(UpdateGameRoomDto dto);

    void deleteGameRoom(UUID id);

    String getLinkToRoom(UUID id);

    ResponseEntity<?> joinRoom(String token);

    /**
     * Метод указывает что игрок готов к игре в своей комнате
     *
     * @param userId Идентификатор пользователя
     * @param gameRoomId Идентификатор игровой комнаты
     */
    void readyToGame(UUID userId, UUID gameRoomId);
}
