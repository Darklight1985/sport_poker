package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserIsPlayerOrCreateRoomHandler extends RoomHandler<UUID> {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UUID... uuids) {
        String user = keycloakUserService.getCurrentUser();
        if (user == null) {
            bindingResult.rejectValue(ErrorCodes.FIELD_IS_NULL, "user.not.found");
        }
        UUID roomId = uuids[0];

        if (roomId == null) {
            bindingResult.rejectValue(ErrorCodes.FIELD_IS_NULL, "user.not.found");
        }

        if (bindingResult.hasErrors()) {
            return;
        }

        UUID userId = UUID.fromString(user);

        if (!gameRoomRepository.userFromThisRoom(userId, roomId)) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Пользователь %s не является участником комнаты %s".formatted(user, roomId));
        }
    }
}
