package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "ДТО, описывающий основную информацию для создания игровой комнаты")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameRoomDto {

    @Schema(description = "Название игровой комнаты")
    private String name;
}
