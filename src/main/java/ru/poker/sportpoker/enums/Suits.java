package ru.poker.sportpoker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Suits {

    CLUBS("Трефы"),
    DIAMONDS("Бубны"),
    HEARTS("Червы"),
    SPADES("Пики");

    private final String description;
}
