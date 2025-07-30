package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserIsPlayerRoomHandler extends RoomHandler<UUID> {

    private final GameRoomRepository gameRoomRepository;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UUID... uuids) {
        if (uuids == null || uuids.length == 0) {
            bindingResult.rejectValue("uuid", "must not be empty");
            return;
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        UUID roomId = uuids[0];
        UUID userId = uuids[1];

        if (roomId == null) {
            bindingResult.rejectValue(ErrorCodes.FIELD_IS_NULL, "user.not.found");
        }

        if (bindingResult.hasErrors()) {
            return;
        }

        if (!gameRoomRepository.userIsPlayerRoom(userId, roomId)) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Пользователь %s не является участником комнаты %s".formatted(userId, uuids));
        }
    }
}
