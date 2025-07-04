package ru.poker.sportpoker.validate;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;

@Component
@Slf4j
public class UserLoginHandler extends UserHandler<UserLoginDto> {
    @Override
    protected void handleSpecifics(UserLoginDto userLoginDto, BindingResult bindingResult) {
        if (userLoginDto.getUsername() == null) {
            bindingResult.reject("Login is null", "Необходимо задать никнейм");
        }
        if (userLoginDto.getPassword() == null) {
            bindingResult.reject("Password is null", "Необходимо задать пароль");
        }
    }
}
