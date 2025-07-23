package ru.poker.sportpoker.validate.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;

@Component
@Slf4j
public class UserLoginHandler extends UserHandler<UserLoginDto> {
    @Override
    protected void handleSpecifics(BindingResult bindingResult, UserLoginDto... dtos) {
        UserLoginDto userLoginDto = dtos[0];

        if (userLoginDto.getUsername() == null) {
            bindingResult.reject("Login is null", "Необходимо задать никнейм");
        } else {
            if (userLoginDto.getUsername().length() < 6) {
                bindingResult.reject("Login small", "Слишком короткое имя");
            }
        }
        if (userLoginDto.getPassword() == null) {
            bindingResult.reject("Password is null", "Необходимо задать пароль");
        }
    }
}
