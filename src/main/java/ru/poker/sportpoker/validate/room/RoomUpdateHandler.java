package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.repository.GameRoomRepository;

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

        if (updateGameRoomDto.getName() != null && !gameRoomRepository.existsByName(updateGameRoomDto.getName(), updateGameRoomDto.getId())) {
            bindingResult.reject("Field is exists", "Комната с именем %s уже существует".formatted(updateGameRoomDto.getName()));
        }
    }
}
