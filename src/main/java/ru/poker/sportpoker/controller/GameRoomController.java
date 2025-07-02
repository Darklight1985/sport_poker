package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.service.GameRoomService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @Operation(description = "Создание игровой комнаты игроком")
    @PostMapping()
    public ResponseEntity<Void> createGameRoom(@RequestBody CreateGameRoomDto dto) {
        gameRoomService.createGameRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "Обновление параметров игровой комнаты игроком")
    @PutMapping()
    public ResponseEntity<Void> updateGameRoom(@RequestBody UpdateGameRoomDto dto) {
        gameRoomService.updateGameRoom(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(description = "Получение данных об игровой комнате")
    @GetMapping("/{id}")
    public ResponseEntity<GameRoomView> getGameRoom(@PathVariable UUID id) {
        GameRoomView gameRoomView = gameRoomService.getGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRoomView);
    }

    @Operation(description = "Получение данных об игровых комнатах")
    @GetMapping("")
    public ResponseEntity<List<GameRoomView>> getGameRooms() {
        List<GameRoomView> gameRoomViews = gameRoomService.getGameRooms();
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRoomViews);
    }


    @Operation(description = "Генерация ссылки для входа в игровую комнату")
    @GetMapping("/{id}/link")
    public String getLinkRoom(@PathVariable UUID id) {
        return gameRoomService.getLinkToRoom(id);
    }

    @Operation(description = "Вход в игровую комнату по токену")
    @PutMapping("/join/{token}")
    public ResponseEntity<?> joinRoom(@Parameter(description = "Токен для входа в комнату по приглашению")@PathVariable String token) {
        return gameRoomService.joinRoom(token);
    }

    @Operation(description = "Удаление игровой комнаты")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGameRoom(@Parameter(description = "Идентификатор комнаты") @PathVariable UUID id) {
        gameRoomService.deleteGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "Принятие от игрока готовности к игре")
    @PostMapping("/{id}/ready")
    public ResponseEntity<Boolean> readyToGame(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(gameRoomService.readyToGame(id));
    }

    @Operation(description = "Покинуть игровую комнату")
    @PostMapping("/{id}/left")
    public void leftRoom(@PathVariable UUID id) {
        //TODO нужна проверка что пользователь в комнате и что комната не в активной фазе
        gameRoomService.leftRoom();
    }

    @Operation(description = "Удалить игрока из игровой комнаты")
    @PostMapping("/{id}/kick/{userId}")
    public void kickRoom(@PathVariable UUID id, @PathVariable UUID userId) {
        //TODO нужна проверка что тек пользователь админ а выкидываемый в той же комнате и комната не в активной фазе
        gameRoomService.kickFromRoom(userId);
    }
}
