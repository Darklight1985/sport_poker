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
import ru.poker.sportpoker.utils.TokenUtils;
import ru.poker.sportpoker.validate.errors.ErrorCodes;
import ru.poker.sportpoker.validate.room.PlayerHandler;
import ru.poker.sportpoker.validate.room.RoomActiveHandler;
import ru.poker.sportpoker.validate.room.RoomCreateExistHandler;
import ru.poker.sportpoker.validate.room.RoomCreateHandler;
import ru.poker.sportpoker.validate.room.RoomUpdateHandler;
import ru.poker.sportpoker.validate.room.RoomValidator;
import ru.poker.sportpoker.validate.room.UserIsCreatorHandler;
import ru.poker.sportpoker.validate.room.UserIsPlayerHandler;
import ru.poker.sportpoker.validate.room.UserIsPlayerOrCreateRoomHandler;
import ru.poker.sportpoker.validate.room.UserIsPlayerRoomHandler;

import java.lang.reflect.Field;
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
    private PlayerHandler playerHandler;

    private TokenUtils tokenUtils;

    private RoomValidator roomValidator;

    private final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");

    private final static UUID ROOM_ID = UUID.randomUUID();
    private static final String ROOM_NAME = randomAlphabetic(8);
    private static final Integer GAME_TIME = Integer.valueOf(10);
    private static final String USER_ID = UUID.randomUUID().toString();
    private final List<StatusGame> statusGameList = List.of(StatusGame.PREP, StatusGame.PLAY);
    private final List<StatusGame> statusGameList2 = List.of(StatusGame.PLAY, StatusGame.END);


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
        playerHandler = new PlayerHandler(gameRoomRepository, keycloakUserService);

        roomValidator = new RoomValidator(roomCreateHandler, roomCreateExistHandler, roomUpdateHandler,
                userIsCreatorHandler, userIsPlayerHandler, userIsPlayerOrCreateRoomHandler, userIsPlayerRoomHandler,
                roomActiveHandler, playerHandler);
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

        UpdateGameRoomDto updateGameRoomDto = new UpdateGameRoomDto();

        @BeforeEach
        void init() {
            updateGameRoomDto.setId(ROOM_ID);
            updateGameRoomDto.setName(ROOM_NAME);
            updateGameRoomDto.setGameTime(GAME_TIME);
        }

        @Test
        @DisplayName("Если пользователь задал корректно все данные, комната будет создана успешно")
        public void test_0() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            Mockito.when(gameRoomRepository.existsByName(ROOM_NAME, ROOM_ID)).thenReturn(false);
            roomValidator.validateUpdateGameRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если комната с указанным именем уже существует, будет выдана ошибка с соответствующим кодом")
        public void test_2() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            Mockito.when(gameRoomRepository.existsByName(ROOM_NAME, ROOM_ID)).thenReturn(true);
            roomValidator.validateUpdateGameRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.ENTITY_ALREADY_EXISTS, bindingResult);
        }

        @Test
        @DisplayName("Если пользователь не является создателем комнаты, будет выдана ошибка с соответствующим кодом")
        public void test_3() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(false);
            roomValidator.validateUpdateGameRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }

        @Test
        @DisplayName("Если обновляемая комната находится в активном статусе 'Игра' или в статуче 'Игра окончена', будет выдана ошибка" +
                " с соответствующим кодом.")
        public void test_4() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            Mockito.when(gameRoomRepository.existsByName(ROOM_NAME, ROOM_ID)).thenReturn(false);
            Mockito.when(gameRoomRepository.roomInStatus(Mockito.eq(ROOM_ID), Mockito.eq(statusGameList2))).thenReturn(true);
            roomValidator.validateUpdateGameRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }

        @ParameterizedTest(name = "Если {0}, то будет добавлена ошибка с соответствующим кодом")
        @MethodSource("provideDataForConstraintTests")
        void test_1 (Consumer<UpdateGameRoomDto> consumer, String errorCode) {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            consumer.accept(updateGameRoomDto);
            roomValidator.validateUpdateGameRoom(updateGameRoomDto, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(errorCode, bindingResult);
        }

        private static Stream<Arguments> provideDataForConstraintTests() {
            return Stream.of(
                    Arguments.of(Named.of("задать пустую строку вместо имени комнаты",
                            createArg(dto -> dto.setName(""))), ErrorCodes.FIELD_IS_BLANK),
                    Arguments.of(Named.of("задать пробельную строку вместе имени комнаты",
                            createArg(dto -> dto.setName("   "))), ErrorCodes.FIELD_IS_BLANK),
                    Arguments.of(Named.of("задать слишком мало времени для игры",
                            createArg(dto -> dto.setGameTime(2))), ErrorCodes.VALUE_CONSTRAINT_VIOLATION),
                    Arguments.of(Named.of("задать слишком много времени для игры",
                            createArg(dto -> dto.setGameTime(61))), ErrorCodes.VALUE_CONSTRAINT_VIOLATION)
            );
        }

        private static Consumer<UpdateGameRoomDto> createArg (Consumer<UpdateGameRoomDto> consumer) {
            return consumer;
        }
    }

    @Nested
    @DisplayName("При получении активной ссылки на комнату :")
    class ValidateGeneralLinkToRoomTest {

        @Test
        @DisplayName("Если пользователь задал корректно все данные, комната будет создана успешно")
        public void test_0() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            roomValidator.validateGenerateLinkToGameRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если пользователь не является создателем указанной комнаты, то будет выдана ошибка с соответствующим кодом")
        public void test_2() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(false);
            roomValidator.validateGenerateLinkToGameRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }

        @Test
        @DisplayName("Если комната на которую пытаемся получить ссылку находится в активном статусе 'Игра' или в статусе" +
                " 'Игра окончена', будет выдана ошибка с соответствующим кодом.")
        public void test_4() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            Mockito.when(gameRoomRepository.roomInStatus(Mockito.eq(ROOM_ID), Mockito.eq(statusGameList2))).thenReturn(true);
            roomValidator.validateGenerateLinkToGameRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }
    }

    @Nested
    @DisplayName("При получении информации о комнате:")
    class ValidateGetRoomTest {

        @Test
        @DisplayName("Если пользователь является участником комнаты, то получение информации пройдет успешно")
        public void test_0() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userFromThisRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            roomValidator.validateGetRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если пользователь не является участником комнаты, то получим ошибку с соответствующим кодом")
        public void test_1() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userFromThisRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(false);
            roomValidator.validateGetRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }
    }

    @Nested
    @DisplayName("При входе в комнату по токену :")
    class ValidateJoinRoomTest {

        private String linkWithToken = null;

        @BeforeEach
        void init() {
            Field secretKeyField;
            Field address;
            Field port;
            try {
                secretKeyField = TokenUtils.class.getDeclaredField("secretKey");
                address = TokenUtils.class.getDeclaredField("address");
                port = TokenUtils.class.getDeclaredField("port");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            secretKeyField.setAccessible(true);
            address.setAccessible(true);
            port.setAccessible(true);
            try {
                secretKeyField.set(tokenUtils, "this-is-secret-only-for-you-loves");
                address.set(tokenUtils, "localhost");
                port.set(tokenUtils, "8083");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            linkWithToken = TokenUtils.getLinkWithToken(ROOM_ID);
        }

        @Test
        @DisplayName("Если пользователь уже является участником какой-то активной комнаты, то поулчим ошибку с соответствующим кодом")
        public void test_0() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userHasRoom(Mockito.eq(UUID.fromString(USER_ID)), Mockito.eq(statusGameList))).thenReturn(false);
            String token = linkWithToken.substring(linkWithToken.lastIndexOf("/") + 1);
            roomValidator.validateJoinRoom(token, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если пользователь не является создателем указанной комнаты, то будет выдана ошибка с соответствующим кодом")
        public void test_2() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userHasRoom(Mockito.eq(UUID.fromString(USER_ID)), Mockito.eq(statusGameList))).thenReturn(true);
            String token = linkWithToken.substring(linkWithToken.lastIndexOf("/") + 1);

            roomValidator.validateJoinRoom(token, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }

        @Test
        @DisplayName("Если комната в которую пользователь пытается зайти находится в активном статусе 'Игра' или в статусе" +
                " 'Игра окончена', будет выдана ошибка с соответствующим кодом.")
        public void test_4() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userHasRoom(Mockito.eq(UUID.fromString(USER_ID)), Mockito.eq(statusGameList))).thenReturn(false);
            Mockito.when(gameRoomRepository.roomInStatus(Mockito.eq(ROOM_ID), Mockito.eq(statusGameList2))).thenReturn(true);
            String token = linkWithToken.substring(linkWithToken.lastIndexOf("/") + 1);

            roomValidator.validateJoinRoom(token, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }
    }

    @Nested
    @DisplayName("При получении активной ссылки на комнату :")
    class ValidateDeleteRoomTest {

        @Test
        @DisplayName("Если пользователь задал корректно все данные, комната будет создана успешно")
        public void test_0() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            roomValidator.validateDeleteGameRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если пользователь не является создателем указанной комнаты, то будет выдана ошибка с соответствующим кодом")
        public void test_2() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(false);
            roomValidator.validateDeleteGameRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }

        @Test
        @DisplayName("Если комната на которую пытаемся получить ссылку находится в активном статусе 'Игра' или в статусе" +
                " 'Игра окончена', будет выдана ошибка с соответствующим кодом.")
        public void test_4() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userIsCreatorRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            Mockito.when(gameRoomRepository.roomInStatus(Mockito.eq(ROOM_ID), Mockito.eq(statusGameList2))).thenReturn(true);
            roomValidator.validateDeleteGameRoom(ROOM_ID, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }
    }

    @Nested
    @DisplayName("При отправке запроса о готовности к игре :")
    class ValidateReadyToGameTest {

        @Test
        @DisplayName("Если пользователь является участником комнаты, то получение информации пройдет успешно")
        public void test_0() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userFromThisRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            roomValidator.validateReadyToGame(ROOM_ID, bindingResult);
            Assertions.assertEquals(0, bindingResult.getAllErrors().size());
        }

        @Test
        @DisplayName("Если пользователь не является участником комнаты, то получим ошибку с соответствующим кодом")
        public void test_1() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userFromThisRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(false);
            roomValidator.validateReadyToGame(ROOM_ID, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }

        @Test
        @DisplayName("Если комната находится в статусе Игры или игра уже завершена, то получим ошибку с соответствующим кодом")
        public void test_2() {
            Mockito.when(keycloakUserService.getCurrentUser()).thenReturn(USER_ID);
            Mockito.when(gameRoomRepository.userFromThisRoom(UUID.fromString(USER_ID), ROOM_ID)).thenReturn(true);
            Mockito.when(gameRoomRepository.roomInStatus(Mockito.eq(ROOM_ID), Mockito.eq(statusGameList2))).thenReturn(true);
            roomValidator.validateReadyToGame(ROOM_ID, bindingResult);
            Assertions.assertEquals(1, bindingResult.getAllErrors().size());
            CommonValidationTestUtil.assertErrorCodeEquals(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, bindingResult);
        }
    }

}
