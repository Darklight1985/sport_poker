package ru.poker.sportpoker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class UserInfo {

    private UUID userId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private boolean ready = false;
}
