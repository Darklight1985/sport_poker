package ru.poker.sportpoker;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.poker.sportpoker.config.KeycloakProperties;
import ru.poker.sportpoker.dto.UserInfo;
import ru.poker.sportpoker.mapper.UserMapper;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.utils.TestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeycloakUserServiceTest {

    @Mock
    private KeycloakProperties keycloakProperties;
    @Mock
    private Keycloak keycloak;

    private KeycloakProperties.Credentials credentials = Mockito.mock(KeycloakProperties.Credentials.class);

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private UsersResource mockUsersResource = Mockito.mock(UsersResource.class);

    private Response mockResponse = Mockito.mock(Response.class);

    private RealmResource mockRealm = Mockito.mock(RealmResource.class);

    private UserResource userResource = Mockito.mock(UserResource.class);
    private UserResource userResource2 = Mockito.mock(UserResource.class);

    private KeycloakUserService keycloakUserService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID USER_ID_2 = UUID.randomUUID();
    private final String username1 = randomAlphabetic(6);
    private final String firstName1 = randomAlphabetic(8);
    private final String lastName1 = randomAlphabetic(10);
    private final String email1 = randomAlphabetic(6);
    private final String username2 = randomAlphabetic(6);
    private final String firstName2 = randomAlphabetic(8);
    private final String lastName2 = randomAlphabetic(10);
    private final String email2 = randomAlphabetic(6);

    private static Keycloak keycloak1 = mock(Keycloak.class);
    private static KeycloakBuilder keycloakBuilder = mock(KeycloakBuilder.class);

    static {
        try (MockedStatic<KeycloakBuilder> mockedStatic = mockStatic(KeycloakBuilder.class)) {
            mockedStatic.when(KeycloakBuilder::builder).thenReturn(keycloakBuilder);
        }
    }


    private UserRepresentation userRepresentation1 = TestUtils.getUserRepresentation(USER_ID, username1, firstName1, lastName1, email1);
    private UserRepresentation userRepresentation2 = TestUtils.getUserRepresentation(USER_ID_2, username2, firstName2, lastName2, email2);

    @BeforeEach
    public void setUp() {
        keycloakProperties.setCredentials(credentials);
        keycloakUserService = new KeycloakUserService(keycloakProperties, userMapper, keycloak);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При регистрации нового пользователя :")
    class RegistrationUserTest {

    @Test
    @DisplayName(" если пользователь новый, то регистрация пройдет успешно. ")
    public void testCreateUser_Success() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        String firstName = "John";
        String lastName = "Doe";

        when(keycloakProperties.getRealm()).thenReturn("keycloak");
        when(credentials.getSecret()).thenReturn("secret");

        when(keycloak.realm(anyString())).thenReturn(mockRealm);
        when(mockRealm.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.create(any())).thenReturn(mockResponse);
        when(mockUsersResource.get(anyString())).thenReturn(userResource);
        when(mockResponse.getStatus()).thenReturn(201);
        try {
            when(mockResponse.getLocation()).thenReturn(new URI("/path/to/user"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        keycloakUserService.createUser(username, email, password, firstName, lastName);
        verify(userResource).resetPassword(any());
    }
}

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При получении информации о пользователях :")
    class GetUsersInfoTest {

        @Test
        @DisplayName(" если указать идентификаторы пользователей, то получим всю информацию по ним")
        public void testGetUsersInfo_Success() {
            Collection<UUID> userIds = Set.of(USER_ID, USER_ID_2);

            when(keycloakProperties.getRealm()).thenReturn("keycloak");
            when(keycloak.realm(anyString())).thenReturn(mockRealm);
            when(mockRealm.users()).thenReturn(mockUsersResource);
            when(mockUsersResource.get(String.valueOf(USER_ID))).thenReturn(userResource);
            when(mockUsersResource.get(String.valueOf(USER_ID_2))).thenReturn(userResource2);
            when(userResource.toRepresentation()).thenReturn(userRepresentation1);
            when(userResource2.toRepresentation()).thenReturn(userRepresentation2);

            Set<UserInfo> result = keycloakUserService.getUsersInfo(userIds);

            assertEquals(2, result.size());
            UserInfo userInfo = result.stream().filter(u -> u.getUserId().equals(USER_ID)).findFirst().get();
            UserInfo userInfo2 = result.stream().filter(u -> u.getUserId().equals(USER_ID_2)).findFirst().get();

            assertEquals(userInfo.getEmail(), email1);
            assertEquals(userInfo2.getEmail(), email2);
            assertEquals(userInfo.getFirstName(), firstName1);
            assertEquals(userInfo2.getLastName(), lastName2);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При получении информации о пользователе :")
    class GetUserInfoTest {
        @Test
        public void testGetUserInfo_Success() {
            when(keycloakProperties.getRealm()).thenReturn("keycloak");
            when(keycloak.realm(anyString())).thenReturn(mockRealm);
            when(mockRealm.users()).thenReturn(mockUsersResource);
            when(mockUsersResource.get(String.valueOf(USER_ID))).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(userRepresentation1);

            UserInfo result = keycloakUserService.getUserInfo(USER_ID);

            assertEquals(result.getEmail(), email1);
            assertEquals(result.getUsername(), username1);
            assertEquals(result.getFirstName(), firstName1);
            assertEquals(result.getLastName(), lastName1);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @DisplayName("При получении идентификатор текущего пользователя :")
    class GetCurrentUserTest {
        @Test
        public void testGetCurrentUser_Authenticated() {
            SecurityContext securityContext = mock(SecurityContext.class);
            JwtAuthenticationToken tokenA = mock(JwtAuthenticationToken.class);

            when(securityContext.getAuthentication()).thenReturn(tokenA);
            when(tokenA.getPrincipal()).thenReturn("user");
            when(tokenA.getTokenAttributes()).thenReturn(Map.of("sub", "12345"));
            SecurityContextHolder.setContext(securityContext);

            String userId = keycloakUserService.getCurrentUser();

            assertEquals("12345", userId);
        }
    }
}

