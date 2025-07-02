package ru.poker.sportpoker.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    @Column(name = "players_id")
    private UUID playersId;

    private boolean ready;

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
        gameRoom.getPlayers().add(this);
    }

    public void deleteGameRoom() {
        gameRoom.getPlayers().remove(this);
        this.gameRoom = null;
    }
}
