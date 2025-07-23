package ru.poker.sportpoker.validate.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomValidator {

    private final RoomCreateHandler roomCreateHandler;
    private final RoomCreateExistHandler roomCreateExistHandler;
    private final RoomUpdateHandler roomUpdateHandler;
    private final PlayerHandler playerHandler;
    private final UserIsCreatorHandler userIsCreatorHandler;
    private final UserIsPlayerHandlerByToken userIsPlayerHandlerByToken;
    private final UserIsPlayerHandler userIsPlayerHandler;
    private final UserIsPlayerOrCreateRoomHandler userIsPlayerOrCreateRoomHandler;
    private final UserIsPlayerRoomHandler userIsPlayerRoomHandler;


    public void validateCreateRoom (CreateGameRoomDto dto, BindingResult bindingResult) {
        roomCreateHandler.handleSpecifics(bindingResult, dto);
        roomCreateExistHandler.handleSpecifics(bindingResult, dto);
        playerHandler.handle(bindingResult);
    }

    public void validateUpdateGameRoom (UpdateGameRoomDto dto, BindingResult bindingResult) {
        roomUpdateHandler.handleSpecifics(bindingResult, dto);
        userIsCreatorHandler.handleSpecifics(bindingResult, dto.getId());
        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }

    public void validateGenerateLinkToGameRoom (UUID roomId, BindingResult bindingResult) {
        userIsCreatorHandler.handleSpecifics(bindingResult, roomId);
        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }

    public void validateJoinRoom (String token, BindingResult bindingResult) {
        userIsPlayerHandlerByToken.handleSpecifics(bindingResult, token);
        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }

    public void validateDeleteGameRoom (UUID roomId, BindingResult bindingResult) {
        userIsCreatorHandler.handleSpecifics(bindingResult, roomId);

        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }

    public void validateReadyToGame (UUID roomId, BindingResult bindingResult) {
       userIsPlayerOrCreateRoomHandler.handleSpecifics(bindingResult, roomId);

        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }

    public void validateLeftGameRoom (UUID roomId, BindingResult bindingResult) {
        userIsPlayerHandler.handleSpecifics(bindingResult, roomId);

        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }

    public void validateKickPlayer (UUID roomId, UUID userId, BindingResult bindingResult) {
          userIsCreatorHandler.handleSpecifics(bindingResult, roomId);
          userIsPlayerRoomHandler.handle(bindingResult, roomId, userId);
        //TODO добавить что нельзя удалить комнату игра в которой уже закончена, или начата
    }
}
