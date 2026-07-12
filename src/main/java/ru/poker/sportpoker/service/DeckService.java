package ru.poker.sportpoker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.Card;
import ru.poker.sportpoker.enums.CardColor;
import ru.poker.sportpoker.enums.Cards;
import ru.poker.sportpoker.enums.Suits;

import java.util.*;

/**
 * Сервис управления колодой карт.
 * Колода содержит 54 уникальные карты (52 стандартные + 2 джокера).
 * Когда колода пуста, она автоматически пересоздается и перемешивается.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeckService {

    private final Random random = new Random();

    /**
     * Создает полную колоду из 54 карт (52 стандартные + 2 джокера).
     * Каждая карта уникальна.
     */
    public List<Card> createFullDeck() {
        List<Card> deck = new ArrayList<>();

        for (Suits suit : Suits.values()) {
            for (Cards rank : Cards.values()) {
                if (rank != Cards.JOKER) {
                    deck.add(new Card(suit, rank));
                }
            }
        }

        Card redJoker = new Card(null, Cards.JOKER);
        redJoker.setColor(CardColor.RED);
        deck.add(redJoker);

        Card blackJoker = new Card(null, Cards.JOKER);
        blackJoker.setColor(CardColor.BLACK);
        deck.add(blackJoker);

        return deck;
    }

    /**
     * Перемешивает колоду случайным образом.
     */
    public void shuffle(List<Card> deck) {
        Collections.shuffle(deck, random);
        log.debug("Колода перемешана. Карт: {}", deck.size());
    }

    /**
     * Выдает карту сверху колоды.
     * Если колода пуста — пересоздает и перемешивает.
     */
    public Card dealCard(List<Card> deck) {
        if (deck.isEmpty()) {
            log.info("Колода пуста. Пересоздаем и перемешиваем.");
            deck.addAll(createFullDeck());
            shuffle(deck);
        }

        Card card = deck.remove(deck.size() - 1);
        log.debug("Выдана карта: {}", card);
        return card;
    }

    /**
     * Проверяет, пуста ли колода.
     */
    public boolean isEmpty(List<Card> deck) {
        return deck.isEmpty();
    }

    /**
     * Возвращает количество карт в колоде.
     */
    public int size(List<Card> deck) {
        return deck.size();
    }
}
