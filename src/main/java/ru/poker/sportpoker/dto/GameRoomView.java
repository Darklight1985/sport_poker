package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Schema(description = "Основные данные по игровой комнате")
@Getter
@Setter
public class GameRoomView extends GameRoomShortView {

    @Schema(description = "Информация о создателе комнаты")
    private PlayerShortInfo creator;

    @Schema(description = "Информация обо всех игроках комнаты")
    private Set<PlayerShortInfo> players;

    @Schema(description = "Сколько времени на игру осталось")
    private Integer minutesLeft;
}
