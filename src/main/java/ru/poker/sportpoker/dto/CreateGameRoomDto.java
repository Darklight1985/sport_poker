package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "ДТО, описывающий основную информацию для создания игровой комнаты")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameRoomDto {

    @Schema(description = "Название игровой комнаты")
    @NotBlank
    private String name;

    @Schema(description = "Время игры")
    @NotNull
    private Integer gameTime;
}
