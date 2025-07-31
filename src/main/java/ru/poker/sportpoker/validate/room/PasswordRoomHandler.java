package ru.poker.sportpoker.validate.room;


import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PasswordRoomHandler extends RoomHandler<String> {

    private final GameRoomRepository gameRoomRepository;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, String... args) {
        if (args == null || args.length == 0) {
            bindingResult.rejectValue(ErrorCodes.FIELD_IS_NULL, "Идентификатор комнаты и пароль должны быть заданы");
            return;
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        String roomId = args[0];
        String password = args[1];

        if (roomId == null) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Не задан идентификатор комнаты");
        }

        if (password == null || password.isBlank()) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Не задан пароль");
        }

        if (bindingResult.hasErrors()) {
            return;
        }

        GameRoom gameRoom = gameRoomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new NotFoundException("Комната не найдена"));
        if (!password.equals(gameRoom.getPassword())) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Пароль указан не верно");
        }
    }
}
