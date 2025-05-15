package ru.poker.sportpoker.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import ru.poker.sportpoker.enums.StatusGame;

import java.time.LocalDateTime;
import java.util.UUID;

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

}
