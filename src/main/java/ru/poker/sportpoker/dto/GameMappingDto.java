package ru.poker.sportpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.enums.Suits;

import java.util.Map;

/**
 * DTO для передачи маппинга мастей к упражнениям.
 * Например: {SPADES: "Подтягивания", HEARTS: "Отжимания", ...}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameMappingDto {

    /**
     * Маппинг: масть -> упражнение
     */
    private Map<Suits, Exercises> suitToExercises;
}
