package ru.poker.sportpoker.event;

import lombok.Data;

import java.util.UUID;

@Data
public class TimerTickEvent {

    private UUID roomId;
    private int minutesLeft;

    public TimerTickEvent(UUID roomId, int minutesLeft) {
        this.roomId = roomId;
        this.minutesLeft = minutesLeft;
    }
}
