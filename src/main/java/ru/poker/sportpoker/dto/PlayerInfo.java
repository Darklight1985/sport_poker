package ru.poker.sportpoker.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
public class PlayerInfo extends PlayerShortInfo {

    private String email;
}
