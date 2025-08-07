package ru.poker.sportpoker.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.CommonValidationTestUtil;
import ru.poker.sportpoker.validate.MultipartfileHandler;
import ru.poker.sportpoker.validate.errors.ErrorCodes;
import ru.poker.sportpoker.validate.room.UserIsHimselfHandler;
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

    private UserIsHimselfHandler userIsHimselfHandler;

    private MultipartfileHandler multipartfileHandler;

    private final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");

    private static final String USERNAME = randomAlphabetic(8);
    private static final String SHORT_USERNAME = randomAlphabetic(5);
    private static final String PASSWORD = randomAlphabetic(8);
    private static final String EMAIL = randomAlphabetic(8);


    @BeforeEach
    void setUp() {
        userLoginHandler = new UserLoginHandler();
        userRegistrationHandler = new UserRegistrationHandler();
        userCreateHandler = new UserCreateHandler(keycloakUserService);

        userValidator = new UserValidator(userLoginHandler, userRegistrationHandler, userCreateHandler,
                userIsHimselfHandler, multipartfileHandler);
    }

    @Nested
    @DisplayName("При входе пользователя в приложение :")
    class ValidateLoginUserTest {

        UserLoginDto userLoginDto = new UserLoginDto();

        @BeforeEach
        void init() {
            userLoginDto.setUsername(USERNAME);
            userLoginDto.setPassword(PASSWORD);
        }

        @Test
        @DisplayName("Если пользователь задал корректно все данные, вход в приложение пройдет успешно")
        public void validateForRegisterUser() {
            userValidator.validateLogin(userLoginDto, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
        @MethodSource("provideDataForConstraintTests")
        void validateForLoginUser (Consumer<UserLoginDto> consumer, String errorCode) {
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
                            createArg(dto -> dto.setUsername(SHORT_USERNAME))), ErrorCodes.FIELD_TOO_SHORT)
            );
        }

        private static Consumer<UserLoginDto> createArg (Consumer<UserLoginDto> consumer) {
            return consumer;
        }

    }

    @Nested
    @DisplayName("При регистрации нового пользователя :")
    class ValidateRegisterUserTest {

        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();

        @BeforeEach
        void init() {
            userRegistrationDto.setUsername(USERNAME);
            userRegistrationDto.setPassword(PASSWORD);
            userRegistrationDto.setEmail(EMAIL);
        }

        @Test
        @DisplayName("Если пользователь задал всю информацию корректно, то ошибок не будет")
        public void test_0() {
            Mockito.when(keycloakUserService.userExists(USERNAME)).thenReturn(false);
            Mockito.when(keycloakUserService.userMailExists(EMAIL)).thenReturn(true);
            userValidator.validateRegistration(userRegistrationDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если пользователь задал имя которое уже существует, регистрация пройдет с ошибкой")
        public void test_1() {
            Mockito.when(keycloakUserService.userExists(USERNAME)).thenReturn(true);
            userValidator.validateRegistration(userRegistrationDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            Assertions.assertEquals(ErrorCodes.ENTITY_ALREADY_EXISTS, bindingResult.getAllErrors().get(0).getCode());
        }

        @Test
        @DisplayName("Если пользователь задал email который уже существует, регистрация пройдет с ошибкой")
        public void test_2() {
            Mockito.when(keycloakUserService.userExists(USERNAME)).thenReturn(false);
            Mockito.when(keycloakUserService.userMailExists(EMAIL)).thenReturn(true);
            userValidator.validateRegistration(userRegistrationDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            Assertions.assertEquals(ErrorCodes.ENTITY_ALREADY_EXISTS, bindingResult.getAllErrors().get(0).getCode());
        }

        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
        @MethodSource("provideDataForConstraintTests")
        void validateRegistrationUser (Consumer<UserRegistrationDto> consumer, String errorCode) {
            consumer.accept(userRegistrationDto);
            userValidator.validateRegistration(userRegistrationDto, bindingResult);
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
                            createArg(dto -> dto.setUsername(SHORT_USERNAME))), ErrorCodes.FIELD_TOO_SHORT),
                    Arguments.of(Named.of("задать пробельную строку вместо email",
                            createArg(dto -> dto.setEmail("  "))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать null вместе email",
                            createArg(dto -> dto.setEmail(null))), ErrorCodes.FIELD_IS_NULL)
            );
        }

        private static Consumer<UserRegistrationDto> createArg(Consumer<UserRegistrationDto> consumer) {
            return consumer;
        }
    }
}
