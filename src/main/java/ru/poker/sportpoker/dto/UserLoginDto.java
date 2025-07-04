package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "ДТО, описывающий основные данные для входа пользователя")
@Getter
@Setter
public class UserLoginDto implements UserDto {

    @Schema(description = "Никнейм пользоватя")
    private String username;

    @Schema(description = "Пароль нового пользователя")
    private String password;
}
