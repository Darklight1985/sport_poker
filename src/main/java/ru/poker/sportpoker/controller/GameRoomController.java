package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomShortView;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.service.GameRoomService;
import ru.poker.sportpoker.validate.room.RoomValidator;
import ru.poker.sportpoker.validate.ValidationException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/room")
public class GameRoomController {

    private final GameRoomService gameRoomService;
    private final RoomValidator roomValidator;

    @Operation(description = "Создание игровой комнаты игроком")
    @PostMapping()
    public ResponseEntity<Void> createGameRoom(@RequestBody CreateGameRoomDto dto, BindingResult bindingResult) {
        roomValidator.validateCreateRoom(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        gameRoomService.createGameRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "Обновление параметров игровой комнаты игроком")
    @PutMapping()
    public ResponseEntity<Void> updateGameRoom(@RequestBody UpdateGameRoomDto dto, BindingResult bindingResult) {
        roomValidator.validateUpdateGameRoom(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        gameRoomService.updateGameRoom(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(description = "Получение данных об игровой комнате")
    @GetMapping("/{id}")
    public ResponseEntity<GameRoomView> getGameRoom(@PathVariable UUID id, BindingResult bindingResult) {
        roomValidator.validateGetRoom(id, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        GameRoomView gameRoomView = gameRoomService.getGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRoomView);
    }

    @Operation(description = "Получение данных об игровых комнатах")
    @GetMapping("")
    public ResponseEntity<Page<GameRoomShortView>> getGameRooms(@PageableDefault Pageable pageable,
                                                                @RequestParam(required = false) String name,
                                                                @RequestParam(required = false) StatusGame statusGame) {
        Page<GameRoomShortView> gameRoomViews = gameRoomService.getGameRooms(pageable, statusGame, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRoomViews);
    }

    @Operation(description = "Генерация ссылки для входа в игровую комнату")
    @GetMapping("/{id}/link")
    public String getLinkRoom(@PathVariable UUID id, BindingResult bindingResult) {
        roomValidator.validateGenerateLinkToGameRoom(id, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        return gameRoomService.getLinkToRoom(id);
    }

    @Operation(description = "Вход в игровую комнату по токену")
    @PutMapping("/join/{token}")
    public ResponseEntity<?> joinRoom(@Parameter(description = "Токен для входа в комнату по приглашению")@PathVariable String token, BindingResult bindingResult) {
        roomValidator.validateJoinRoom(token, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        return gameRoomService.joinRoomByToken(token);
    }

    @Operation(description = "Вход в игровую комнату по паролю")
    @PutMapping("{id}/join/")
    public ResponseEntity<?> joinRoomByPassword(@Parameter(description = "Токен для входа в комнату по приглашению")
                                                    @PathVariable UUID id,
                                                @RequestBody String password,
                                                BindingResult bindingResult) {
        roomValidator.validateJoinRoom(password, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        return gameRoomService.joinRoomByPassword(id);
    }

    @Operation(description = "Удаление игровой комнаты")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGameRoom(@Parameter(description = "Идентификатор комнаты") @PathVariable UUID id, BindingResult bindingResult) {
        roomValidator.validateDeleteGameRoom(id, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        gameRoomService.deleteGameRoom(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "Принятие от игрока готовности к игре")
    @PostMapping("/{id}/ready")
    public ResponseEntity<Boolean> readyToGame(@PathVariable UUID id, BindingResult bindingResult) {
        roomValidator.validateReadyToGame(id, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(gameRoomService.readyToGame(id));
    }

    @Operation(description = "Покинуть игровую комнату")
    @PostMapping("/{id}/left")
    public void leftRoom(@PathVariable UUID id, BindingResult bindingResult) {
        roomValidator.validateLeftGameRoom(id, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        gameRoomService.leftRoom();
    }

    @Operation(description = "Удалить игрока из игровой комнаты")
    @PostMapping("/{id}/kick/{userId}")
    public void kickRoom(@PathVariable UUID id, @PathVariable UUID userId, BindingResult bindingResult) {
        roomValidator.validateKickPlayer(id, userId, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        gameRoomService.kickFromRoom(userId);
    }
}
