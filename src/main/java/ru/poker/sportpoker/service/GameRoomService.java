package ru.poker.sportpoker.service;

import org.springframework.http.ResponseEntity;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;

import java.util.List;
import java.util.UUID;

public interface GameRoomService {

    /**
     * Создание игровой комнаты
     * @param dto
     */
    void createGameRoom(CreateGameRoomDto dto);

    /**
     * Получить информацию об игровой комнате по ее идентификатору
     *
     * @param id Идентификатор игровой комнаты
     * @return Представление с информацией об игровой комнате
     */
    GameRoomView getGameRoom(UUID id);

    /**
     * Получить информацию обо всех игровых комнатах
     *
     * @return Список представлений с информацией об игровых комнатах
     */
    List<GameRoomView> getGameRooms();

    /**
     * Метод обновления данных об игровой комнате
     *
     * @param dto ДТО содежащий информацию для обновления игровой комнаты
     */
    void updateGameRoom(UpdateGameRoomDto dto);

    /**
     * Метод для удаления игровой комнаты
     *
     * @param id
     */
    void deleteGameRoom(UUID id);

    /**
     * Метод получения ссылки для входа в игровую комнату
     *
     * @param id
     * @return
     */
    String getLinkToRoom(UUID id);

    /**
     * Метод для входа в игровую комнату по токену
     *
     * @param token
     * @return
     */
    ResponseEntity<?> joinRoom(String token);

    /**
     * Метод указывает что игрок готов к игре в своей комнате
     *
     * @param gameRoomId Идентификатор игровой комнаты
     */
    boolean readyToGame(UUID gameRoomId);

    /**
     * Метод позволяющий текущему игроку покинуть игровую комнату
     *
     */
    void leftRoom();

    /**
     * Метод выкидывания игрока из его игровой комнаты
     *
     * @param playerId
     */
    void kickFromRoom(UUID playerId);
}
