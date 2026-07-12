package ru.poker.sportpoker.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UuidGenerator;
import ru.poker.sportpoker.enums.Suits;

import java.util.*;

@Entity
@Getter
@Setter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_room_players")
public class GameRoomPlayer {

    /**
     * Идентификатор комнаты
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    @Column(name = "players_id")
    private UUID playersId;

    private boolean ready;

    private int score;

    @Transient
    private Card currentCard;

    @Transient
    private Map<Suits, Boolean> completedExercises; // Для отслеживания выполнения упражнений по мастям (для джокеров)

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
        gameRoom.getGameRoomPlayers().add(this);
    }

    public void deleteGameRoom() {
        gameRoom.getGameRoomPlayers().remove(this);
        this.gameRoom = null;
    }
}
