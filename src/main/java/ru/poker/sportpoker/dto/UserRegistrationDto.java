package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "ДТО, описывающий основные данные для регистрации нового пользователя")
@Getter
@Setter
public class UserRegistrationDto extends UserLoginDto {

    @Schema(description = "Почтовый ящик пользователя")
    private String email;

    @Schema(description = "Имя")
    private String firstName;

    @Schema(description = "Фамилия")
    private String lastName;
}
