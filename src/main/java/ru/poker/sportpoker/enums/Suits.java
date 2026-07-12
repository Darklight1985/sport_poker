package ru.poker.sportpoker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Suits {

    CLUBS("Трефы", CardColor.BLACK),
    DIAMONDS("Бубны", CardColor.RED),
    HEARTS("Червы", CardColor.RED),
    SPADES("Пики", CardColor.BLACK);

    private final String description;
    private final CardColor color;
}
