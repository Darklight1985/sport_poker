package ru.poker.sportpoker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.poker.sportpoker.enums.CardColor;
import ru.poker.sportpoker.enums.Cards;
import ru.poker.sportpoker.enums.Suits;

/**
 * Объект карты в колоде.
 * Каждая карта уникальна и содержит масть, достоинство и очки.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    private Suits suit;
    private Cards rank;
    private int points;
    private CardColor color;
    private boolean joker;

    public Card(Suits suit, Cards rank) {
        this.suit = suit;
        this.rank = rank;
        this.points = rank.getPoint();
        this.color = suit.getColor();
        this.joker = rank == Cards.JOKER;
    }

    @Override
    public String toString() {
        if (joker) {
            return color.name() + " Joker";
        }
        return rank.getDescription() + " " + suit.getDescription();
    }
}
