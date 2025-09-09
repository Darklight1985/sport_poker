package ru.poker.sportpoker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Exercises {

    PULL_UPS("Подтягивания"),
    PUSH_UP("Отжимания"),
    SQUAT("Приседания"),
    DEADLIFT("Становая тяга"),
    BURPEE("Берпи"),
    KETTLEBELL_SWING("Мах гирей"),
    BOX_JUMP("Запрыгивание на ящик"),
    HANDSTAND("Стойка на руках"),
    ROPE_CLIMB("Лазание по канату");

    private final String description;
}
