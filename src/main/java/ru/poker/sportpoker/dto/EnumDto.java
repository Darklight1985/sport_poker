package ru.poker.sportpoker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema (description = "ДТО, описывающий основную информацию из списка для выбора")
public record EnumDto (String name, String description) {}
