package ru.poker.sportpoker.validate.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

@Component
@Slf4j
public class UserLoginHandler extends UserHandler<UserLoginDto> {
    @Override
    protected void handleSpecifics(BindingResult bindingResult, UserLoginDto... dtos) {
        UserLoginDto userLoginDto = dtos[0];

        if (userLoginDto.getUsername() == null || userLoginDto.getUsername().isBlank()) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Необходимо задать никнейм");
        } else {
            if (userLoginDto.getUsername().length() < 6) {
                bindingResult.reject(ErrorCodes.FIELD_TOO_SHORT, "Слишком короткое имя");
            }
        }
        if (userLoginDto.getPassword() == null || userLoginDto.getPassword().isBlank()) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Необходимо задать пароль");
        }
    }
}
