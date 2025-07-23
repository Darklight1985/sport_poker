package ru.poker.sportpoker.validate.room;

;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.KeycloakUserService;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserIsPlayerHandler extends RoomHandler<UUID> {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UUID... uuids) {
        if (uuids == null || uuids.length == 0) {
            bindingResult.reject("uuid", "must not be empty");
            return;
        }

        UUID roomId = uuids[0];
        String user = keycloakUserService.getCurrentUser();
        if (user == null) {
            bindingResult.reject("user", "user.not.found");
        }
        if (roomId == null) {
            bindingResult.reject("roomId", "user.not.found");
        }

        if (bindingResult.hasErrors()) {
            return;
        }

        UUID userId = UUID.fromString(user);

        if (!gameRoomRepository.userIsPlayerRoom(userId, roomId)) {
            bindingResult.reject("user", "Пользователь %s не является участником комнаты %s".formatted(user, uuids));
        }
    }
}
