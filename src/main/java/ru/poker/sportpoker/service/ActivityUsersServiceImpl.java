package ru.poker.sportpoker.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.domain.GameRoomPlayer;
import ru.poker.sportpoker.domain.Card;
import ru.poker.sportpoker.dto.CardDto;
import ru.poker.sportpoker.dto.GameMappingDto;
import ru.poker.sportpoker.dto.GameRankingDto;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.enums.Suits;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.event.GameEndEvent;
import ru.poker.sportpoker.event.TimerTickEvent;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.utils.TokenUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@AllArgsConstructor
@Service
@Data
@Slf4j
public class ActivityUsersServiceImpl implements ActivityUserService {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final DeckService deckService;
    private final ExerciseMapper exerciseMapper;
    private final GameRoomSseService sseService;
    private final MinioFileService minioFileService;

    //TODO по этой мапе можно проводить валидацию что если комната уже здесь то все выйти из игры уже не можешь
    private static final Map<UUID, GameRoom> activeRoom = new ConcurrentHashMap<>();
    private static final Lock lock = new ReentrantLock();

    public GameRoom getActiveRoom(UUID roomId) {
        return activeRoom.get(roomId);
    }

    public void activeRoom(GameRoom gameRoom) {
        gameRoom.letsPlay(eventPublisher);
        activeRoom.put(gameRoom.getId(), gameRoom);
    }

    @EventListener
    public void endGame(GameEndEvent event) {
        GameRoom gameRoom = activeRoom.get(event.getRoomId());
        if (gameRoom != null) {
            log.debug("Игра в комнате {} окончена", gameRoom.getName());
            gameRoom.setStatus(StatusGame.END);
            activeRoom.remove(event.getRoomId());
            gameRoomRepository.save(gameRoom);
            
            // Отправляем RANKING всем игрокам
            GameRankingDto ranking = getGameRanking(event.getRoomId());
            sseService.sendToRoom(event.getRoomId(), "RANKING", ranking);
            
            // Удаляем эмиттеры комнаты
            sseService.removeRoomSubscribers(event.getRoomId());
        }
    }

    @EventListener
    public void timerTick(TimerTickEvent event) {
        // Отправляем TIMER событие всем игрокам комнаты
        sseService.sendToRoom(event.getRoomId(), "TIMER", Map.of(
                "minutesLeft", event.getMinutesLeft()
        ));
    }

    @Override
    public void initializeGame(UUID roomId) {
        GameRoom gameRoom = activeRoom.get(roomId);
        if (gameRoom == null) {
            throw new IllegalArgumentException("Комната не найдена в активных играх: " + roomId);
        }

        // 1. Создаем и перемешиваем колоду
        List<ru.poker.sportpoker.domain.Card> deck = deckService.createFullDeck();
        deckService.shuffle(deck);
        gameRoom.setDeck(deck);
        gameRoom.setCardsDealt(0);

        // 2. Генерируем маппинг упражнений
        Map<Suits, Exercises> mapping = exerciseMapper.mapExercises(gameRoom.getExercises());
        gameRoom.setExerciseMapping(mapping);

        // 3. Сбрасываем очки и раздаем карты игрокам
        for (GameRoomPlayer player : gameRoom.getGameRoomPlayers()) {
            player.setScore(0);
            player.setCurrentCard(deckService.dealCard(deck, gameRoom.getPlayedCards()));
            player.setCompletedExercises(new java.util.HashMap<>());
            log.debug("Игроку {} выдана карта: {}", player.getPlayersId(), player.getCurrentCard());
        }

        log.info("Игра инициализирована в комнате {}: колода из {} карт, маппинг {}", 
                roomId, gameRoom.getDeck().size(), mapping);

        // 4. Отправляем MAPPING всем игрокам
        sseService.sendToRoom(roomId, "MAPPING", new GameMappingDto(mapping));

        // 5. Отправляем CARD каждому игроку
        for (GameRoomPlayer player : gameRoom.getGameRoomPlayers()) {
            String imageUrl = generateCardImageUrl(player.getCurrentCard());
            CardDto cardDto = CardDto.fromCard(player.getCurrentCard(), imageUrl);
            sseService.sendToPlayer(roomId, player.getPlayersId(), "CARD", cardDto);
        }
    }

    @Override
    public GameMappingDto getGameMapping() {
        String userId = keycloakUserService.getCurrentUser();
        UUID playerId = UUID.fromString(userId);

        // Находим активную комнату игрока
        for (GameRoom gameRoom : activeRoom.values()) {
            if (StatusGame.PLAY.equals(gameRoom.getStatus())) {
                return GameMappingDto.builder()
                        .suitToExercises(gameRoom.getExerciseMapping())
                        .build();
            }
        }
        throw new IllegalArgumentException("Активная игра не найдена");
    }

    @Override
    public GameRankingDto getGameRanking(UUID roomId) {
        GameRoom gameRoom = activeRoom.get(roomId);
        if (gameRoom == null) {
            throw new IllegalArgumentException("Комната не найдена: " + roomId);
        }

        List<GameRoomPlayer> sortedPlayers = new ArrayList<>(gameRoom.getGameRoomPlayers());
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        List<GameRankingDto.PlayerRankingDto> rankings = IntStream.range(0, sortedPlayers.size())
                .mapToObj(rank -> GameRankingDto.PlayerRankingDto.builder()
                        .rank(rank + 1)
                        .playerId(sortedPlayers.get(rank).getPlayersId())
                        .score(sortedPlayers.get(rank).getScore())
                        .build())
                .toList();

        return GameRankingDto.builder()
                .rankings(rankings)
                .build();
    }

    /**
     * Генерирует URL изображения карты.
     */
    private String generateCardImageUrl(Card card) {
        if (card == null) {
            return null;
        }
        if (card.isJoker()) {
            return minioFileService.getJokerUrl(card.getColor().name());
        }
        String rankName = card.getRank().name().toLowerCase();
        String suitName = card.getSuit().name().toLowerCase();
        return minioFileService.getCardUrl(rankName, suitName);
    }
}
