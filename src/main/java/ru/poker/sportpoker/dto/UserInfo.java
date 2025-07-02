package ru.poker.sportpoker.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
public class UserInfo extends UserShortInfo {

    private String email;

    private String firstName;

    private String lastName;
}
