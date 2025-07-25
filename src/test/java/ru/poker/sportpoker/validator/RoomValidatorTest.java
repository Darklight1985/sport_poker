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
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.enums.StatusGame;
import ru.poker.sportpoker.repository.GameRoomRepository;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.CommonValidationTestUtil;
import ru.poker.sportpoker.validate.errors.ErrorCodes;
import ru.poker.sportpoker.validate.room.RoomActiveHandler;
import ru.poker.sportpoker.validate.room.RoomCreateExistHandler;
import ru.poker.sportpoker.validate.room.RoomCreateHandler;
import ru.poker.sportpoker.validate.room.RoomUpdateHandler;
import ru.poker.sportpoker.validate.room.RoomValidator;
import ru.poker.sportpoker.validate.room.UserIsCreatorHandler;
import ru.poker.sportpoker.validate.room.UserIsPlayerHandler;
import ru.poker.sportpoker.validate.room.UserIsPlayerOrCreateRoomHandler;
import ru.poker.sportpoker.validate.room.UserIsPlayerRoomHandler;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@ExtendWith(MockitoExtension.class)
public class RoomValidatorTest {

    @Mock
    private KeycloakUserService keycloakUserService;
    @Mock
    private GameRoomRepository gameRoomRepository;

    private RoomCreateHandler roomCreateHandler;
    private RoomCreateExistHandler roomCreateExistHandler;
    private RoomUpdateHandler roomUpdateHandler;
    private UserIsCreatorHandler userIsCreatorHandler;
    private UserIsPlayerHandler userIsPlayerHandler;
    private UserIsPlayerOrCreateRoomHandler userIsPlayerOrCreateRoomHandler;
    private UserIsPlayerRoomHandler userIsPlayerRoomHandler;
    private RoomActiveHandler roomActiveHandler;

    private RoomValidator roomValidator;

    private final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");

    private static final String ROOM_NAME = randomAlphabetic(8);
    private static final Integer GAME_TIME = Integer.valueOf(10);
    private static final String USER_ID = UUID.randomUUID().toString();
    private final List<StatusGame> statusGameList = List.of(StatusGame.PREP, StatusGame.PLAY);


    @BeforeEach
    void setUp() {
        roomCreateHandler = new RoomCreateHandler();
        roomCreateExistHandler = new RoomCreateExistHandler(gameRoomRepository, keycloakUserService);
        roomUpdateHandler = new RoomUpdateHandler(gameRoomRepository);
        userIsCreatorHandler = new UserIsCreatorHandler(gameRoomRepository, keycloakUserService);
        userIsPlayerHandler = new UserIsPlayerHandler(gameRoomRepository, keycloakUserService);
        userIsPlayerOrCreateRoomHandler = new UserIsPlayerOrCreateRoomHandler(gameRoomRepository, keycloakUserService);
        userIsPlayerRoomHandler = new UserIsPlayerRoomHandler(gameRoomRepository);
        roomActiveHandler = new RoomActiveHandler(gameRoomRepository);

        roomValidator = new RoomValidator(roomCreateHandler, roomCreateExistHandler, roomUpdateHandler,
                userIsCreatorHandler, userIsPlayerHandler, userIsPlayerOrCreateRoomHandler, userIsPlayerRoomHandler,
                roomActiveHandler);
    }

    @Nested
    @DisplayName("При создании пользователем новой комнаты :")
    class ValidateCreateRoomTest {

        CreateGameRoomDto createGameRoomDto = new CreateGameRoomDto();

        @BeforeEach
        void init() {
            createGameRoomDto.setName(ROOM_NAME);
            createGameRoomDto.setGameTime(GAME_TIME);
        }

        @Test
        @DisplayName("Если пользователь задал корректно все данные, вход в приложение пройдет успешно")
        public void validateForRegisterUser() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userHasRoom(UUID.fromString(USER_ID), statusGameList)).thenReturn(false);
            Mockito.when(gameRoomRepository.existsByName(ROOM_NAME)).thenReturn(false);
            roomValidator.validateCreateRoom(createGameRoomDto, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
        @MethodSource("provideDataForConstraintTests")
        void validateForLoginUser (Consumer<CreateGameRoomDto> consumer, String errorCode) {
            consumer.accept(createGameRoomDto);
            roomValidator.validateCreateRoom(createGameRoomDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(errorCode, bindingResult);
        }

        private static Stream<Arguments> provideDataForConstraintTests() {
            return Stream.of(
                    Arguments.of(Named.of("задать null вместе username",
                            createArg(dto -> dto.setName(null))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пустую строку вместо пароля",
                            createArg(dto -> dto.setName(""))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пробельную строку вместе пароля",
                            createArg(dto -> dto.setName("   "))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пустую строку вместо username",
                            createArg(dto -> dto.setGameTime(2))), ErrorCodes.VALUE_CONSTRAINT_VIOLATION),
                    Arguments.of(Named.of("задать пробельную строку вместо username",
                            createArg(dto -> dto.setGameTime(61))), ErrorCodes.VALUE_CONSTRAINT_VIOLATION)
            );
        }

        private static Consumer<CreateGameRoomDto> createArg (Consumer<CreateGameRoomDto> consumer) {
            return consumer;
        }
    }

    @Nested
    @DisplayName("При обновлении комнаты пользователем :")
    class ValidateUpdateRoomTest {

        private final static UUID ROOM_ID = UUID.randomUUID();
        UpdateGameRoomDto updateGameRoomDto = new UpdateGameRoomDto();

        @BeforeEach
        void init() {
            updateGameRoomDto.setId(ROOM_ID);
            updateGameRoomDto.setName(ROOM_NAME);
            updateGameRoomDto.setGameTime(GAME_TIME);
        }

        @Test
        @DisplayName("Если пользователь задал корректно все данные, вход в приложение пройдет успешно")
        public void validateForRegisterUser() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userHasRoom(UUID.fromString(USER_ID), statusGameList)).thenReturn(false);
            Mockito.when(gameRoomRepository.existsByName(ROOM_NAME)).thenReturn(false);
            roomValidator.validateCreateRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
        @MethodSource("provideDataForConstraintTests")
        void validateForLoginUser (Consumer<UpdateGameRoomDto> consumer, String errorCode) {
            consumer.accept(updateGameRoomDto);
            roomValidator.validateUpdateGameRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(errorCode, bindingResult);
        }

        private static Stream<Arguments> provideDataForConstraintTests() {
            return Stream.of(
                    Arguments.of(Named.of("задать null вместе username",
                            createArg(dto -> dto.setName(null))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пустую строку вместо пароля",
                            createArg(dto -> dto.setName(""))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пробельную строку вместе пароля",
                            createArg(dto -> dto.setName("   "))), ErrorCodes.FIELD_IS_NULL),
                    Arguments.of(Named.of("задать пустую строку вместо username",
                            createArg(dto -> dto.setGameTime(2))), ErrorCodes.VALUE_CONSTRAINT_VIOLATION),
                    Arguments.of(Named.of("задать пробельную строку вместо username",
                            createArg(dto -> dto.setGameTime(61))), ErrorCodes.VALUE_CONSTRAINT_VIOLATION)
            );
        }

        private static Consumer<UpdateGameRoomDto> createArg (Consumer<UpdateGameRoomDto> consumer) {
            return consumer;
        }
    }

//    @Nested
//    @DisplayName("При регистрации нового пользователя :")
//    class ValidateRegisterUserTest {
//
//        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
//
//        @BeforeEach
//        void init() {
//            userRegistrationDto.setUsername(USERNAME);
//            userRegistrationDto.setPassword(PASSWORD);
//            userRegistrationDto.setEmail(EMAIL);
//        }
//
//        @Test
//        @DisplayName("Если пользователь задал всю информацию корректно, то ошибок не будет")
//        public void test_0() {
//            Mockito.when(keycloakUserService.userExists(USERNAME)).thenReturn(false);
//            Mockito.when(keycloakUserService.userMailExists(EMAIL)).thenReturn(true);
//            roomValidator.validateRegistration(userRegistrationDto, bindingResult);
//            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
//        }
//
//        @Test
//        @DisplayName("Если пользователь задал имя которое уже существует, регистрация пройдет с ошибкой")
//        public void validateForRegisterUser() {
//              Mockito.when(keycloakUserService.userExists(USERNAME)).thenReturn(true);
//              roomValidator.validateRegistration(userRegistrationDto, bindingResult);
//              Assertions.assertEquals(1, bindingResult.getAllErrors().size());
//        }
//
//        @Test
//        @DisplayName("Если пользователь задал email который уже существует, регистрация пройдет с ошибкой")
//        public void validateForRegisterUser2() {
//            Mockito.when(keycloakUserService.userExists(USERNAME)).thenReturn(false);
//            Mockito.when(keycloakUserService.userMailExists(EMAIL)).thenReturn(true);
//            roomValidator.validateRegistration(userRegistrationDto, bindingResult);
//            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
//        }
//
//        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
//        @MethodSource("provideDataForConstraintTests")
//        void validateRegistrationUser (Consumer<UserRegistrationDto> consumer, String errorCode) {
//            consumer.accept(userRegistrationDto);
//            roomValidator.validateRegistration(userRegistrationDto, bindingResult);
//            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
//            CommonValidationTestUtil.assertErrorCodeEquals(errorCode, bindingResult);
//        }
//
//        private static Stream<Arguments> provideDataForConstraintTests() {
//            return Stream.of(
//                    Arguments.of(Named.of("задать null вместе username",
//                            createArg(dto -> dto.setUsername(null))), ErrorCodes.FIELD_IS_NULL),
//                    Arguments.of(Named.of("задать пустую строку вместо пароля",
//                            createArg(dto -> dto.setPassword(""))), ErrorCodes.FIELD_IS_NULL),
//                    Arguments.of(Named.of("задать пробельную строку вместе пароля",
//                            createArg(dto -> dto.setPassword("   "))), ErrorCodes.FIELD_IS_NULL),
//                    Arguments.of(Named.of("задать пустую строку вместо username",
//                            createArg(dto -> dto.setUsername(""))), ErrorCodes.FIELD_IS_NULL),
//                    Arguments.of(Named.of("задать пробельную строку вместо username",
//                            createArg(dto -> dto.setUsername("  "))), ErrorCodes.FIELD_IS_NULL),
//                    Arguments.of(Named.of("задать слишком короткое имя",
//                            createArg(dto -> dto.setUsername(SHORT_USERNAME))), ErrorCodes.FIELD_TOO_SHORT),
//                    Arguments.of(Named.of("задать пробельную строку вместо email",
//                            createArg(dto -> dto.setEmail("  "))), ErrorCodes.FIELD_IS_NULL),
//                    Arguments.of(Named.of("задать null вместе email",
//                            createArg(dto -> dto.setEmail(null))), ErrorCodes.FIELD_IS_NULL)
//            );
//        }
//
//        private static Consumer<UserRegistrationDto> createArg(Consumer<UserRegistrationDto> consumer) {
//            return consumer;
//        }
//    }
}
