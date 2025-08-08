package ru.poker.sportpoker.validate.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.validate.MultipartfileHandler;
import ru.poker.sportpoker.validate.ValidationException;
import ru.poker.sportpoker.validate.room.UserIsHimselfHandler;

import java.util.UUID;

@Component
@Slf4j
public class UserValidator {

    private final UserHandler<UserLoginDto> userLoginHandler;
    private final UserRegistrationHandler userRegistrationHandler;
    private final UserCreateHandler userCreateHandler;
    private final UserIsHimselfHandler userIsHimselfHandler;
    private final MultipartfileHandler multipartfileHandler;

    public UserValidator(UserLoginHandler userLoginHandler, UserRegistrationHandler userRegistrationHandler,
                         UserCreateHandler userCreateHandler, UserIsHimselfHandler userIsHimselfHandler, MultipartfileHandler multipartfileHandler) {
        this.userLoginHandler = userLoginHandler;
        this.userRegistrationHandler = userRegistrationHandler;
        this.userCreateHandler = userCreateHandler;
        this.userIsHimselfHandler = userIsHimselfHandler;
        this.multipartfileHandler = multipartfileHandler;
    }

    public void validateLogin(UserLoginDto dto, BindingResult bindingResult) {
        userLoginHandler.handle(bindingResult, dto);
    }

    public void validateRegistration(UserRegistrationDto dto, BindingResult bindingResult) {
        userLoginHandler.handle(bindingResult, dto);
        userRegistrationHandler.handle(bindingResult, dto);
        userCreateHandler.handle(bindingResult, dto);
    }

    public void validatePutAvatar(UUID userId, MultipartFile file, BindingResult bindingResult) {
        userIsHimselfHandler.handle(bindingResult, userId);
        multipartfileHandler.handle(bindingResult, file);
    }

    public void validateGetAvatar(UUID userId, BindingResult bindingResult) {
        userIsHimselfHandler.handle(bindingResult, userId);
    }
}
