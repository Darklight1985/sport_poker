package ru.poker.sportpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO для передачи итогового рейтинга игроков.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRankingDto {

    private List<PlayerRankingDto> rankings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerRankingDto {
        private int rank; // место (1, 2, 3...)
        private UUID playerId;
        private int score;
    }
}
