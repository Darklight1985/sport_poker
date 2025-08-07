package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Основная информация о пользователе в личном кабинете")
public class UserView {

    @Schema(description = "Идентификатор пользователя")
    private UUID userId;

    @Schema(description = "Никнейм пользователя")
    private String username;

    @Schema(description = "Электронная почта пользователя")
    private String email;
}
