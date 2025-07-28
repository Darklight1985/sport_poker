package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomActiveHandler extends RoomHandler<UUID> {

    private final GameRoomRepository gameRoomRepository;

    private final List<StatusGame> statusGameList = List.of(StatusGame.PLAY, StatusGame.END);

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UUID... uuids) {
        if (bindingResult.hasErrors()) {
            return;
        }
        UUID roomId = uuids[0];

        if (gameRoomRepository.roomInStatus(roomId, statusGameList)) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "С указанной комнатой нельзя уже проводить никакие манипуляции");
        }
    }
}
