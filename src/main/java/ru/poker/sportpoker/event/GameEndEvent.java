package ru.poker.sportpoker.event;

import lombok.Data;

import java.util.UUID;

@Data
public class GameEndEvent {

    private UUID roomId;

    public GameEndEvent(UUID roomId) {
        this.roomId = roomId;
    }
}
