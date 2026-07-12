package ru.poker.sportpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.poker.sportpoker.domain.Card;
import ru.poker.sportpoker.enums.CardColor;
import ru.poker.sportpoker.enums.Cards;
import ru.poker.sportpoker.enums.Suits;

/**
 * DTO для передачи информации о карте игроку.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {

    private Suits suit;
    private Cards rank;
    private int points;
    private CardColor color;
    private boolean joker;
    private String imageUrl; // URL изображения карты в MinIO

    /**
     * Создает CardDto из domain Card.
     */
    public static CardDto fromCard(Card card) {
        return CardDto.builder()
                .suit(card.getSuit())
                .rank(card.getRank())
                .points(card.getPoints())
                .color(card.getColor())
                .joker(card.isJoker())
                .build();
    }

    /**
     * Создает CardDto из domain Card с URL изображения.
     */
    public static CardDto fromCard(Card card, String imageUrl) {
        return CardDto.builder()
                .suit(card.getSuit())
                .rank(card.getRank())
                .points(card.getPoints())
                .color(card.getColor())
                .joker(card.isJoker())
                .imageUrl(imageUrl)
                .build();
    }
}
