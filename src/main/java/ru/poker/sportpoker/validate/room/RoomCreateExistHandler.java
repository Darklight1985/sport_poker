package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.KeycloakUserService;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoomCreateExistHandler extends RoomHandler<CreateGameRoomDto> {

    private final GameRoomRepository gameRoomRepository;
    private final KeycloakUserService keycloakUserService;
    private final List<StatusGame> statusGameList = List.of(StatusGame.PREP, StatusGame.PLAY);

    @Override
    protected void handleSpecifics(BindingResult bindingResult, CreateGameRoomDto... dtos) {
        if (bindingResult.hasErrors()) {
            return;
        }
        CreateGameRoomDto createGameRoomDto = dtos[0];

        String user = keycloakUserService.getCurrentUser();
        if (user == null) {
            bindingResult.rejectValue("user", "user.not.found");
        }
        UUID userId = UUID.fromString(user);

        if (gameRoomRepository.userHasRoom(userId, statusGameList)) {
            bindingResult.reject("User has game room", "Пользовать %s уже закрепле за комнатой".formatted(userId));
        }

        if (gameRoomRepository.existsByName(createGameRoomDto.getName())) {
            bindingResult.reject("Room exists", "Комната с именем %s уже существует".formatted(createGameRoomDto.getName()));
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        if (createGameRoomDto.getGameTime() < 5 || createGameRoomDto.getGameTime() > 60) {
            bindingResult.reject("Username is exists", "Время игры должно быть от 5 до 60 минут");
        }
    }
}
