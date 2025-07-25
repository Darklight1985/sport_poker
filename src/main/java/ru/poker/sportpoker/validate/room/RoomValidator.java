package ru.poker.sportpoker.validate.room;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.utils.TokenUtils;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomValidator {

    private final RoomCreateHandler roomCreateHandler;
    private final RoomCreateExistHandler roomCreateExistHandler;
    private final RoomUpdateHandler roomUpdateHandler;
    private final UserIsCreatorHandler userIsCreatorHandler;
    private final UserIsPlayerHandler userIsPlayerHandler;
    private final UserIsPlayerOrCreateRoomHandler userIsPlayerOrCreateRoomHandler;
    private final UserIsPlayerRoomHandler userIsPlayerRoomHandler;
    private final RoomActiveHandler roomActiveHandler;

    public void validateCreateRoom(CreateGameRoomDto dto, BindingResult bindingResult) {
        roomCreateHandler.handle(bindingResult, dto);
        roomCreateExistHandler.handle(bindingResult, dto);
    }

    public void validateUpdateGameRoom(UpdateGameRoomDto dto, BindingResult bindingResult) {
        userIsCreatorHandler.handle(bindingResult, dto.getId());
        roomUpdateHandler.handle(bindingResult, dto);
        roomActiveHandler.handle(bindingResult, dto.getId());
    }

    public void validateGenerateLinkToGameRoom(UUID roomId, BindingResult bindingResult) {
        userIsCreatorHandler.handle(bindingResult, roomId);
        roomActiveHandler.handle(bindingResult, roomId);
    }

    public void validateGetRoom(UUID roomId, BindingResult bindingResult) {
        userIsPlayerOrCreateRoomHandler.handle(bindingResult, roomId);
    }


    public void validateJoinRoom(String token, BindingResult bindingResult) {
        String roomId = null;
        try {
            roomId = TokenUtils.getRoomId(token);
        } catch (JwtException e) {
            bindingResult.reject("invalid_token", "Токен кривой");
        }

        userIsPlayerHandler.handle(bindingResult, UUID.fromString(roomId));
        roomActiveHandler.handle(bindingResult, UUID.fromString(roomId));
    }

    public void validateDeleteGameRoom(UUID roomId, BindingResult bindingResult) {
        userIsCreatorHandler.handle(bindingResult, roomId);
        roomActiveHandler.handle(bindingResult, roomId);
    }

    public void validateReadyToGame(UUID roomId, BindingResult bindingResult) {
        userIsPlayerOrCreateRoomHandler.handle(bindingResult, roomId);
        roomActiveHandler.handle(bindingResult, roomId);
    }

    public void validateLeftGameRoom(UUID roomId, BindingResult bindingResult) {
        userIsPlayerHandler.handle(bindingResult, roomId);
        roomActiveHandler.handle(bindingResult, roomId);
    }

    public void validateKickPlayer(UUID roomId, UUID userId, BindingResult bindingResult) {
        userIsCreatorHandler.handle(bindingResult, roomId);
        userIsPlayerRoomHandler.handle(bindingResult, roomId, userId);
        roomActiveHandler.handle(bindingResult, roomId);
    }
}
