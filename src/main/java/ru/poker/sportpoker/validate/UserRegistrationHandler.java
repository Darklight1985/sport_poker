package ru.poker.sportpoker.validate;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserRegistrationDto;

@Component
@Slf4j
public class UserRegistrationHandler extends UserHandler<UserRegistrationDto> {

    @Override
    protected void handleSpecifics(UserRegistrationDto userRegistrationDto, BindingResult bindingResult) {
        if (userRegistrationDto.getEmail() == null) {
            bindingResult.reject("Email is null", "Укажите электронную почту");
        }
    }
}
