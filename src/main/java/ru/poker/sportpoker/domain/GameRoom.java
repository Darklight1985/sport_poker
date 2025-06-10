package ru.poker.sportpoker.domain;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;
import org.springframework.context.ApplicationEventPublisher;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.event.GameEndEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Entity
@Getter
@Setter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_room")
public class GameRoom {

    @Transient
    private CountDownTimer countDownTimer;

    public void letsPlay(ApplicationEventPublisher publisher) {
        countDownTimer = new CountDownTimer(gameTime, publisher);
        status = StatusGame.PLAY;
    }

    /**
     * Идентификатор комнаты
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
     * Длительность игры в минутах
     */
    @Column(name = "game_time")
    private Integer gameTime;

    /**
     * Статус комнаты
     */
    @Enumerated(EnumType.STRING)
    private StatusGame status = StatusGame.PREP;

    @Column(updatable = false)
    private UUID creator;

    @OneToMany(mappedBy = "gameRoom", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<GameRoomPlayer> players = new HashSet<>();


    public GameRoomPlayer getPlayer (UUID playerId) {
        for (GameRoomPlayer gameRoomPlayer : players) {
            if (gameRoomPlayer.getPlayersId().equals(playerId)) {
                return gameRoomPlayer;
            }
        }
        return null;
    }

    public void removePlayer (GameRoomPlayer gameRoomPlayer) {
        players.remove(gameRoomPlayer);
    }


    @Getter
    @Setter
    public class CountDownTimer {
        private int minutesLeft;
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final ApplicationEventPublisher eventPublisher;

        public CountDownTimer(int seconds, ApplicationEventPublisher eventPublisher) {
            this.minutesLeft = seconds;
            this.eventPublisher = eventPublisher;
            scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
        }

        private void tick() {
            if (minutesLeft > 0) {
                minutesLeft--;
                log.debug("В комнате {} времени осталось - {} минут", name, minutesLeft);
            } else {
                eventPublisher.publishEvent(new GameEndEvent(id));
                scheduler.shutdown();
            }
        }
    }

}
