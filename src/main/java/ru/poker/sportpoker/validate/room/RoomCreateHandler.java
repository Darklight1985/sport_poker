package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.CreateGameRoomDto;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomCreateHandler extends RoomHandler<CreateGameRoomDto> {

    @Override
    protected void handleSpecifics(BindingResult bindingResult, CreateGameRoomDto... dtos) {
        CreateGameRoomDto createGameRoomDto = dtos[0];

        if (createGameRoomDto.getName() == null) {
            bindingResult.reject("Room name exists", "Необходимо задать имя для комнаты");
        }

        if (createGameRoomDto.getGameTime() == null) {
            bindingResult.reject("Field null", "Необходимо задать время игры в комнате");
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        if (createGameRoomDto.getGameTime() < 5 || createGameRoomDto.getGameTime() > 60) {
            bindingResult.reject("Field length not allowed", "Длина игры должна составлять от 5 до 60 минут");
        }
    }
}
