package ru.poker.sportpoker.validate;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.dto.UserRegistrationDto;

@Component
public class UserValidator {

    private final UserHandler<UserLoginDto> userLoginHandler;
    private final UserRegistrationHandler userRegistrationHandler;
    private final UserCreateHandler userCreateHandler;

    public UserValidator(UserLoginHandler userLoginHandler, UserRegistrationHandler userRegistrationHandler,
                         UserCreateHandler userCreateHandler) {
        this.userLoginHandler = userLoginHandler;
        this.userRegistrationHandler = userRegistrationHandler;
        this.userCreateHandler = userCreateHandler;
    }

    public void validateLogin(UserLoginDto dto, BindingResult bindingResult) {
        userLoginHandler.handle(bindingResult, dto);
    }

    public void validateRegistration(UserRegistrationDto dto, BindingResult bindingResult) {
        userLoginHandler.handle(bindingResult, dto);
        userRegistrationHandler.handle(bindingResult, dto);
        userCreateHandler.handle(bindingResult, dto);
    }
}
