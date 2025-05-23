package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.service.GameRoomService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @Operation(description = "Запуск игры")
    @PutMapping("/start")
    public String adminEndpoint() {
        GameRoom room = new GameRoom();
        room.letsPlay(100);
        return "Game room started";
    }

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
    public ResponseEntity<Void> getGameRoom(@PathVariable UUID id) {
        gameRoomService.getGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
}
