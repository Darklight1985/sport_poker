package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.poker.sportpoker.enums.StatusGame;

import java.util.UUID;

@Schema(description = "Основные данные по игровой комнате")
@Getter
@Setter
public class GameRoomShortView {

    @Schema(description = "Идентификатор комнаты")
    private UUID roomId;

    @Schema(description = "Статус комнаты")
    private StatusGame status;

    @Schema(description = "Имя комнаты")
    private String name;

    @Schema(description = "Время на игру")
    private Integer gameTime;
}
