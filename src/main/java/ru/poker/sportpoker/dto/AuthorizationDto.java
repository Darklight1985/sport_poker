package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ДТО описывающий данные для аутентификации и авторизации пользователя")
public record AuthorizationDto ( String username, String password ) {};

