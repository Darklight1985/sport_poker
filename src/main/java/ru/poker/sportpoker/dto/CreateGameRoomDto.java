package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.poker.sportpoker.enums.Exercises;

import java.util.Set;

@Schema(description = "ДТО, описывающий основную информацию для создания игровой комнаты")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameRoomDto {

    @Schema(description = "Название игровой комнаты")
    private String name;

    @Schema(description = "Время игры")

    private Integer gameTime;

    @Schema(description = "Пароль от комнаты")
    private String password;

    @Schema(description = "Упражнения")
    private Set<Exercises> exercises;
}
