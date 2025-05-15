package ru.poker.sportpoker.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class CountDownTimer {
    private long secondsLeft;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CountDownTimer(long seconds) {
        this.secondsLeft = seconds;
        scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        if (secondsLeft > 0) {
            secondsLeft--;
            System.out.println(this.toString() + " " + secondsLeft);
        } else {
            scheduler.shutdown();
        }
    }
}
