package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomUpdateHandler extends RoomHandler<UpdateGameRoomDto> {

    private final GameRoomRepository gameRoomRepository;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UpdateGameRoomDto... dtos) {
        if (bindingResult.hasErrors()) {
            return;
        }
        UpdateGameRoomDto updateGameRoomDto = dtos[0];

        if (updateGameRoomDto.getName() != null && updateGameRoomDto.getName().isBlank()) {
            bindingResult.reject(ErrorCodes.FIELD_IS_BLANK, "name.required");
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        if (updateGameRoomDto.getName() != null && gameRoomRepository.existsByName(updateGameRoomDto.getName(), updateGameRoomDto.getId())) {
            bindingResult.reject(ErrorCodes.ENTITY_ALREADY_EXISTS, "Комната с именем %s уже существует".formatted(updateGameRoomDto.getName()));
        }

        if (updateGameRoomDto.getGameTime() != null && (updateGameRoomDto.getGameTime() < 5 || updateGameRoomDto.getGameTime() > 60)) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Длина игры должна составлять от 5 до 60 минут");
        }
    }
}
