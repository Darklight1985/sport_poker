package ru.poker.sportpoker.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.CommonValidationTestUtil;
import ru.poker.sportpoker.validate.errors.ErrorCodes;
import ru.poker.sportpoker.validate.user.UserCreateHandler;
import ru.poker.sportpoker.validate.user.UserLoginHandler;
import ru.poker.sportpoker.validate.user.UserRegistrationHandler;
import ru.poker.sportpoker.validate.user.UserValidator;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@ExtendWith(MockitoExtension.class)
public class UserValidatorTest {

    @Mock
    private KeycloakUserService keycloakUserService;

    private UserLoginHandler userLoginHandler;

    private UserRegistrationHandler userRegistrationHandler;

    private UserCreateHandler userCreateHandler;

    private UserValidator userValidator;

    private final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");


    @BeforeEach
    void setUp() {
        userLoginHandler = new UserLoginHandler();
        userRegistrationHandler = new UserRegistrationHandler();
        userCreateHandler = new UserCreateHandler(keycloakUserService);

        userValidator = new UserValidator(userLoginHandler, userRegistrationHandler, userCreateHandler);
    }

    @Nested
    @DisplayName("При вызове метода validateForCreateExperiment():")
    class ValidateLoginUserTest {

        UserLoginDto userLoginDto = new UserLoginDto();

        @BeforeEach
        void init() {
            userLoginDto.setUsername("username");
            userLoginDto.setPassword("password");
        }

        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
        @MethodSource("provideDataForConstraintTests")
        void validateForCreateExperiment(Consumer<UserLoginDto> consumer, String errorCode) {
            consumer.accept(userLoginDto);
            userValidator.validateLogin(userLoginDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(errorCode, bindingResult);
        }

        private static Stream<Arguments> provideDataForConstraintTests() {
            return Stream.of(
                    Arguments.of(Named.of("задать null вместе username",
                            createArg(dto -> dto.setUsername(null))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пустую строку вместо пароля",
                            createArg(dto -> dto.setPassword(""))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пробельную строку вместе пароля",
                            createArg(dto -> dto.setPassword("   "))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пустую строку вместо username",
                            createArg(dto -> dto.setUsername(""))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пробельную строку вместо username",
                            createArg(dto -> dto.setUsername("  "))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать слишком короткое имя",
                            createArg(dto -> dto.setUsername(randomAlphabetic(5)))), ErrorCodes.FIELD_TOO_SHORT)
            );
        }

        private static Consumer<UserLoginDto> createArg(Consumer<UserLoginDto> consumer) {
            return consumer;
        }

    }
}
