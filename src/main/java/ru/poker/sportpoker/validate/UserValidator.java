package ru.poker.sportpoker.validate;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.dto.UserRegistrationDto;

@Component
public class UserValidator {

    private final UserHandler<UserLoginDto> userLoginHandler;
    private final UserHandler<UserRegistrationDto> userRegistrationHandler;

    public UserValidator(UserLoginHandler userLoginHandler, UserRegistrationHandler userRegistrationHandler) {
        this.userLoginHandler = userLoginHandler;
        this.userRegistrationHandler = userRegistrationHandler;
    }

    public void validateLogin(UserLoginDto dto, BindingResult bindingResult) {
        userLoginHandler.handle(dto, bindingResult);
    }

    public void validateRegistration(UserRegistrationDto dto, BindingResult bindingResult) {
        userRegistrationHandler.handle(dto, bindingResult);
        userLoginHandler.handle(dto, bindingResult);
    }
}
