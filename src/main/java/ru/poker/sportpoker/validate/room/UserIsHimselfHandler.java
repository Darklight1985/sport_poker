package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserIsHimselfHandler extends RoomHandler<UUID> {

    private final KeycloakUserService keycloakUserService;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UUID... uuids) {
        String user = keycloakUserService.getCurrentUser();
        if (user == null) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Пользователь не аутентфиицирован");
        }
        UUID userId = uuids[0];

        if (!user.equals(userId.toString())) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Текущий пользователь и пользователь в запросе не совпадают");
        }
    }
}
