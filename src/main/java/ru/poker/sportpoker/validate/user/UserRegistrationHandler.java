package ru.poker.sportpoker.validate.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

@Component
@Slf4j
public class UserRegistrationHandler extends UserHandler<UserRegistrationDto> {

    @Override
    protected void handleSpecifics(BindingResult bindingResult, UserRegistrationDto... dtos) {
        UserRegistrationDto userRegistrationDto = dtos[0];

        if (userRegistrationDto.getEmail() == null || userRegistrationDto.getEmail().isBlank()) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Укажите электронную почту");
        }
    }
}
