package ru.poker.sportpoker.domain;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import ru.poker.sportpoker.enums.StatusGame;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_room")
public class GameRoom {

    @Transient
    private CountDownTimer countDownTimer;

    public void letsPlay(int time) {
        countDownTimer = new CountDownTimer(time);
        status = StatusGame.PLAY;
    }

    /**
     * Глобальный идентификатор объекта.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(updatable = false)
    private UUID id;

    /**
     * Дата и время создания комнаты.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;

    /**
     * Дата и время изменения объекта в БД.
     */
    @UpdateTimestamp
    private LocalDateTime updated;

    /**
     * Имя комнаты.
     */
    private String name;

    /**
     * Статус комнаты
     */
    @Enumerated(EnumType.STRING)
    private StatusGame status = StatusGame.PREP;

    @Column(updatable = false)
    private UUID creator;

    @ElementCollection
    @CollectionTable(name = "game_room_players", joinColumns = @JoinColumn(name = "game_room_id"))
    @Column(name = "players_id")
    private Set<UUID> players = new HashSet<>();


    public void addPlayer (UUID id) {
        players.add(id);
    }

    public void removePlayer (UUID id) {
        players.remove(id);
    }

    public void clearPlayers() {
        players.clear();
    }
}
