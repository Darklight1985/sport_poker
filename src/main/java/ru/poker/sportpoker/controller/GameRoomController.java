package ru.poker.sportpoker.controller;

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

    @GetMapping("/start")
    public String adminEndpoint() {
        GameRoom room = new GameRoom();
        room.letsPlay(100);
        return "Game room started";
    }

    @PostMapping()
    public ResponseEntity<Void> createGameRoom(@RequestBody CreateGameRoomDto dto) {
        gameRoomService.createGameRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping()
    public ResponseEntity<Void> updateGameRoom(@RequestBody UpdateGameRoomDto dto) {
        gameRoomService.updateGameRoom(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> getGameRoom(@PathVariable UUID id) {
        gameRoomService.getGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/link")
    public String getLinkRoom(@PathVariable UUID id) {
        return gameRoomService.getLinkToRoom(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGameRoom(@PathVariable UUID id) {
        gameRoomService.deleteGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
