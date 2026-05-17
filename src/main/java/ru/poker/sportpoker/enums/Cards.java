package ru.poker.sportpoker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Cards {

    TWO("Двойка", 2),
    THREE("Тройка", 3),
    FOUR("Четверка", 4),
    FIVE("Пятерка", 5),
    SIX("Шестерка", 6),
    SEVEN("Семерка", 7),
    EIGHT("Восьмерка", 8),
    NINE("Девятка", 9),
    TEN("Десятка", 10),
    JACK("Валет", 11),
    QUEEN("Дама", 12),
    KING("Король", 13),
    ACE("Туз", 14),
    JOKER("Джокер", 15);

    private final String description;

    private final int point;
}
