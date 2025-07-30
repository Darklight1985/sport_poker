package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomCreateHandler extends RoomHandler<CreateGameRoomDto> {

    @Override
    protected void handleSpecifics(BindingResult bindingResult, CreateGameRoomDto... dtos) {
        CreateGameRoomDto createGameRoomDto = dtos[0];

        if (createGameRoomDto.getName() == null || createGameRoomDto.getName().isBlank()) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Необходимо задать имя для комнаты");
        }

        if (createGameRoomDto.getGameTime() == null) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Необходимо задать время игры в комнате");
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        if (createGameRoomDto.getName().length() < 6) {
            bindingResult.reject(ErrorCodes.FIELD_TOO_SHORT, "Имя комнаты должно быть не меньше 6 символов");
        }

        if (createGameRoomDto.getGameTime() < 5 || createGameRoomDto.getGameTime() > 60) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Длина игры должна составлять от 5 до 60 минут");
        }
    }
}
