package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.poker.sportpoker.enums.StatusGame;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Основные данные по игровой комнате")
@Getter
@Setter
public class GameRoomView {

    @Schema(description = "Идентификатор комнаты")
    private UUID roomId;

    @Schema(description = "Статус комнаты")
    private StatusGame status;

    @Schema(description = "Информация о создателе комнаты")
    private UserShortInfo creator;

    @Schema(description = "Информация обо всех игроках комнаты")
    private Set<UserShortInfo> players;

    @Schema(description = "Время на игру")
    private Integer gameTime;

    @Schema(description = "Сколько времени на игру осталось")
    private Integer minutesLeft;
}
