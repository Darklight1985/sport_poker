package ru.poker.sportpoker.service;

import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.GameMappingDto;
import ru.poker.sportpoker.dto.GameRankingDto;
import ru.poker.sportpoker.event.GameEndEvent;

import java.util.Map;
import java.util.UUID;
import ru.poker.sportpoker.enums.Suits;
import ru.poker.sportpoker.enums.Exercises;

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

    /**
     * Инициализация игры: создает колоду, генерирует маппинг мастей к упражнениям,
     * раздает карты игрокам
     *
     * @param roomId Идентификатор игровой комнаты
     */
    void initializeGame(UUID roomId);

    /**
     * Получение маппинга мастей к упражнениям для текущей активной игры
     *
     * @return DTO с маппингом
     */
    GameMappingDto getGameMapping();

    /**
     * Получение итогового рейтинга игроков по окончании игры
     *
     * @param roomId Идентификатор игровой комнаты
     * @return Рейтинг игроков, отсортированный по очкам (по убыванию)
     */
    GameRankingDto getGameRanking(UUID roomId);
}
