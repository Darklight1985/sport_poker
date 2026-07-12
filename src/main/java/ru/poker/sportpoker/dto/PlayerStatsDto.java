package ru.poker.sportpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для передачи статистики игрока.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsDto {

    private UUID playerId;
    private int score;
    private CardDto currentCard;
}
