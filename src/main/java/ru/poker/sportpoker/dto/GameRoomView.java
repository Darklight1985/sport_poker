package ru.poker.sportpoker.dto;

import lombok.Getter;
import lombok.Setter;
import ru.poker.sportpoker.enums.StatusGame;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class GameRoomView {

    private UUID roomId;

    private StatusGame statusGame;

    private UserShortInfo creator;

    private Set<UserShortInfo> players;

    private Long secondLeft;
}
