package ru.poker.sportpoker.validate.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserCreateHandler extends UserHandler<UserRegistrationDto> {

    private final KeycloakUserService keycloakUserService;

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UserRegistrationDto... dtos) {
        if (bindingResult.hasErrors()) {
            return;
        }
        UserRegistrationDto userRegistrationDto = dtos[0];

        if (keycloakUserService.userExists(userRegistrationDto.getUsername())) {
            bindingResult.reject(ErrorCodes.ENTITY_ALREADY_EXISTS, "Пользователь с таким ником уже зарегестрирован");
        }

        if (keycloakUserService.userMailExists(userRegistrationDto.getEmail())) {
            bindingResult.reject(ErrorCodes.ENTITY_ALREADY_EXISTS, "Пользователь с данной электронной почтой уже зарегестрирован");
        }
    }
}
