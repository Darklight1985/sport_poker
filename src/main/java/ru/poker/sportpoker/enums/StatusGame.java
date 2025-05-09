package ru.poker.sportpoker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusGame {

    PREP("Фаза подготовки ингры, сборк игроков"),
    PLAY("Игра началась"),
    END("Игра окончена");

    private final String description;
}
