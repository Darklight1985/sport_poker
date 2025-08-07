package ru.poker.sportpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PlayerShortInfo {

    private UUID userId;

    private String username;

    private boolean ready = false;
}
