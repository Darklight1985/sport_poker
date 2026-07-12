package ru.poker.sportpoker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.enums.Suits;

import java.util.*;

/**
 * Сервис для маппинга мастей карт к упражнениям.
 * Генерирует случайное распределение упражнений по мастям.
 */
@Slf4j
@Service
public class ExerciseMapper {

    /**
     * Генерирует маппинг мастей к упражнениям.
     * Каждая масть получает случайное упражнение из списка доступных.
     *
     * @param exercises - список доступных упражнений для данной комнаты
     * @return маппинг: масть -> упражнение
     */
    public Map<Suits, Exercises> mapExercises(Set<Exercises> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            throw new IllegalArgumentException("Список упражнений не может быть пустым");
        }

        List<Exercises> exerciseList = new ArrayList<>(exercises);
        Map<Suits, Exercises> mapping = new EnumMap<>(Suits.class);

        // Перемешиваем упражнения
        Collections.shuffle(exerciseList);

        // Назначаем каждое упражнение случайной масти
        int exerciseIndex = 0;
        for (Suits suit : Suits.values()) {
            mapping.put(suit, exerciseList.get(exerciseIndex % exerciseList.size()));
            exerciseIndex++;
        }

        log.debug("Сгенерирован маппинг упражнений: {}", mapping);
        return mapping;
    }
}
