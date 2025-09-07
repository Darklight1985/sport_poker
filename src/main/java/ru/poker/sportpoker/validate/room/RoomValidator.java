package ru.poker.sportpoker.validate.room;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.utils.TokenUtils;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.Set;
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
    private final PlayerHandler playerHandler;
    private final PasswordRoomHandler passwordRoomHandler;
    private final ExercisesHandler exercisesHandler;

    public void validateCreateRoom(CreateGameRoomDto dto, BindingResult bindingResult) {
        roomCreateHandler.handle(bindingResult, dto);
        roomCreateExistHandler.handle(bindingResult, dto);
        exercisesHandler.handle(bindingResult, dto.getExercises());
    }

    public void validateUpdateGameRoom(UpdateGameRoomDto dto, BindingResult bindingResult) {
        userIsCreatorHandler.handle(bindingResult, dto.getId());
        roomUpdateHandler.handle(bindingResult, dto);
        roomActiveHandler.handle(bindingResult, dto.getId());
        Set<Exercises> exercises = dto.getExercises();
        if (exercises != null) {
            exercisesHandler.handle(bindingResult, exercises);
        }
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
            bindingResult.reject(ErrorCodes.VALUE_IS_NONPOSITIVE, "Токен кривой");
        }


        playerHandler.handle(bindingResult);
        roomActiveHandler.handle(bindingResult, UUID.fromString(roomId));
    }

    public void validateJoinRoom(UUID roomId, String password, BindingResult bindingResult) {
        playerHandler.handle(bindingResult);
        roomActiveHandler.handle(bindingResult, roomId);
        passwordRoomHandler.handle(bindingResult, String.valueOf(roomId), password);
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
