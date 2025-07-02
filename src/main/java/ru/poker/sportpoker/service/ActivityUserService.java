package ru.poker.sportpoker.service;

import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.event.GameEndEvent;

import java.util.UUID;

public interface ActivityUserService {


    /**
     * Получение информации об игровой комнате, если она в фазе подготовке или игры
     *
     * @param roomId
     * @return
     */
    GameRoom getActiveRoom(UUID roomId);

    /**
     * Поместить игровую комнату в словарь активных когда все игроки станут готовы к игре
     *
     * @param gameRoom
     */
    void activeRoom(GameRoom gameRoom);

    /**
     * Перевод игровой комнаты в фазу окончания игры
     *
     * @param event Событие об окончании игры
     */
    void endGame(GameEndEvent event);
}
