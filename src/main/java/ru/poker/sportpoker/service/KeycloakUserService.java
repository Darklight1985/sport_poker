package ru.poker.sportpoker.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.config.KeycloakProperties;
import ru.poker.sportpoker.dto.UserInfo;
import ru.poker.sportpoker.exception.UserRegistrationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final KeycloakProperties keycloakProperties;

    public void createUser(String username, String email, String password, String firstName, String lastName) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getAuthServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getResource())
                .clientSecret(keycloakProperties.getCredentials().getSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build()) {

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);

            UsersResource userResource = keycloak.realm(keycloakProperties.getRealm()).users();

            try {
                try (Response response = userResource.create(user)) {
                    if (response.getStatus() != 201) {
                        String error = response.readEntity(String.class);
                        System.out.println(error);
                        throw new UserRegistrationException("Ошибка создания пользователя: " + error);
                    } else {
                        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                        CredentialRepresentation passwordCred = new CredentialRepresentation();
                        passwordCred.setTemporary(false);
                        passwordCred.setType(CredentialRepresentation.PASSWORD);
                        passwordCred.setValue(password);

                        keycloak.realm(keycloakProperties.getRealm()).users().get(userId).resetPassword(passwordCred);
                    }
                }
            } catch (WebApplicationException ex) {
                String error = ex.getResponse().readEntity(String.class);
                throw new UserRegistrationException("Ошибка Keycloak: " + error);
            }
        }
    }

    /**
     * Получение информации о списке пользователей по их идентификаторам
     *
     * @param userIds Список идентификаторов пользователей
     * @return Список с информацией о пользователях
     */
    public List<UserInfo> getUsersInfo(Collection<UUID> userIds) {
        List<UserInfo> list = new ArrayList<>();
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getAuthServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getResource())
                .clientSecret(keycloakProperties.getCredentials().getSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build()) {

            UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();

            for (UUID userId : userIds) {
                UserResource userResource = usersResource.get(String.valueOf(userId));
                UserRepresentation userRepresentation = userResource.toRepresentation();

                UserInfo userInfo = UserInfo.builder()
                        .ready(false)
                        .userId(userId)
                        .lastName(userRepresentation.getLastName())
                        .firstName(userRepresentation.getFirstName())
                        .username(userRepresentation.getUsername())
                        .email(userRepresentation.getEmail())
                        .build();

                list.add(userInfo);
            }
        }
        return list;
    }


    public UserInfo getUserInfo(UUID userId) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getAuthServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getResource())
                .clientSecret(keycloakProperties.getCredentials().getSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build()) {

            UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
            UserResource userResource = usersResource.get(String.valueOf(userId));
            UserRepresentation userRepresentation = userResource.toRepresentation();

            return UserInfo.builder()
                    .ready(false)
                    .userId(userId)
                    .lastName(userRepresentation.getLastName())
                    .firstName(userRepresentation.getFirstName())
                    .username(userRepresentation.getUsername())
                    .email(userRepresentation.getEmail())
                    .build();
        }
    }

    /**
     * Получение идентификатора текущего пользователя
     * @return Строку содержащую идентификатор текущего пользователя
     */
    public String getCurrentUser() {
        // Получение текущей аутентификации
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        JwtAuthenticationToken tokenA = (JwtAuthenticationToken) authentication;
        var atr = tokenA.getTokenAttributes();

        // Получение идентификатора пользователя из claims
        String userId = (String) atr.get("sub");
        System.out.println("User ID: " + userId);
        return userId;
    }

    /**
     * Аутентификация пользователя через Keycloak.
     */
    public AccessTokenResponse authenticate(String username, String password) {
        try {
            try (Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakProperties.getAuthServerUrl())
                    .realm(keycloakProperties.getRealm())
                    .clientId(keycloakProperties.getResourceUser())
                    .clientSecret(keycloakProperties.getCredentials().getUserSecret())
                    .grantType(OAuth2Constants.PASSWORD)
                    .username(username)
                    .password(password)
                    .build()) {

                return keycloak.tokenManager().getAccessToken();
            }
        } catch (Exception e) {
            e.printStackTrace(); // Выведет подробности
            throw new RuntimeException("Invalid username or password", e);
        }
    }
}
