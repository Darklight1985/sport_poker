package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.KeycloakUserService;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayerHandler {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final List<StatusGame> statusGameList = List.of(StatusGame.PREP, StatusGame.PLAY);

    protected void handle(BindingResult bindingResult) {
        String user = keycloakUserService.getCurrentUser();
        if (user == null) {
            bindingResult.rejectValue("user", "user.not.found");
        }

        UUID userId = UUID.fromString(user);

        if (gameRoomRepository.userHasRoom(userId, statusGameList)) {
            bindingResult.rejectValue("user", "Пользователь уже закреплен за игровой комнатой");
        }
    }
}
