package ru.poker.sportpoker.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
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
import ru.poker.sportpoker.dto.PlayerInfo;
import ru.poker.sportpoker.exception.AuthentificationException;
import ru.poker.sportpoker.exception.UserRegistrationException;
import ru.poker.sportpoker.mapper.UserMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserService {

    private final KeycloakProperties keycloakProperties;
    private final UserMapper userMapper;
    private final Keycloak keycloak;

    public void createUser(String username, String email, String password) {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setEnabled(true);
            user.setEmailVerified(true);
            user.setRequiredActions(List.of());

            RealmResource realm = keycloak.realm(keycloakProperties.getRealm());
            UsersResource userResource = realm.users();

            try {
                try (Response response = userResource.create(user)) {
                    if (response.getStatus() != 201) {
                        String error = response.readEntity(String.class);
                        throw new UserRegistrationException("Ошибка создания пользователя: " + error);
                    } else {
                        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                        // Установить пароль (не временный)
                        CredentialRepresentation passwordCred = new CredentialRepresentation();
                        passwordCred.setTemporary(false);
                        passwordCred.setType(CredentialRepresentation.PASSWORD);
                        passwordCred.setValue(password);
                        userResource.get(userId).resetPassword(passwordCred);

                        // Снять все required actions (realm может добавлять их автоматически)
                        UserRepresentation createdUser = userResource.get(userId).toRepresentation();
                        createdUser.setRequiredActions(List.of());
                        createdUser.setEmailVerified(true);
                        userResource.get(userId).update(createdUser);
                    }
                }
            } catch (WebApplicationException ex) {
                String error = ex.getResponse().readEntity(String.class);
                throw new UserRegistrationException("Ошибка Keycloak: " + error);
            }
    }

    /**
     * Получение информации о списке пользователей по их идентификаторам
     *
     * @param userIds Список идентификаторов пользователей
     * @return Список с информацией о пользователях
     */
    public Set<PlayerInfo> getUsersInfo(Collection<UUID> userIds) {
        Set<PlayerInfo> list = new HashSet<>();
            UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();

            for (UUID userId : userIds) {
                UserResource userResource = usersResource.get(String.valueOf(userId));
                UserRepresentation userRepresentation = userResource.toRepresentation();
                PlayerInfo playerInfo = userMapper.getUserInfo(userRepresentation);

                list.add(playerInfo);
            }
        return list;
    }


    public PlayerInfo getUserInfo(UUID userId) {
            return userMapper.getUserInfo(getUserRepresentation(userId));
    }

    public UserRepresentation getUserRepresentation(UUID userId) {
        UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
        UserResource userResource = usersResource.get(String.valueOf(userId));
        return userResource.toRepresentation();
    }

    /**
     * Получение идентификатора текущего пользователя
     * @return Строку содержащую идентификатор текущего пользователя
     */
    public String getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        JwtAuthenticationToken tokenA = (JwtAuthenticationToken) authentication;
        var atr = tokenA.getTokenAttributes();

        String userId = (String) atr.get("sub");
        log.debug("userId: {}", userId);
        return userId;
    }

    /**
     * Аутентификация пользователя через Keycloak.
     */
    public AccessTokenResponse authenticate(String username, String password) {
        clearRequiredActions(username);
        try {
            log.debug("Attempting authentication for user '{}' with clientId='{}', serverUrl='{}', realm='{}'",
                    username, keycloakProperties.getResourceUser(),
                    keycloakProperties.getAuthServerUrl(), keycloakProperties.getRealm());
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
        } catch (jakarta.ws.rs.BadRequestException e) {
            String responseBody = "";
            try {
                Response errorResponse = e.getResponse();
                if (errorResponse != null) {
                    errorResponse.bufferEntity();
                    responseBody = errorResponse.readEntity(String.class);
                }
            } catch (Exception ignored) {
                // не удалось прочитать тело ответа
            }
            log.error("Keycloak authentication failed (400) for user '{}', clientId='{}'. Response body: {}",
                    username, keycloakProperties.getResourceUser(), responseBody);
            throw new AuthentificationException("Invalid username or password");
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user '{}': {}", username, e.getMessage(), e);
            throw new AuthentificationException("Invalid username or password");
        }
    }

    public boolean userExists(String username) {
        UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
        return !usersResource.search(username).isEmpty();
    }

    public boolean userMailExists(String email) {
        UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
        return !usersResource.searchByEmail(email, true).isEmpty();
    }

    /**
     * Снять все required actions у пользователя перед аутентификацией,
     * чтобы избежать ошибки "Account is not fully set up".
     */
    private void clearRequiredActions(String username) {
        try {
            UsersResource usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
            java.util.List<UserRepresentation> users = usersResource.search(username, true);
            if (users.isEmpty()) {
                return;
            }
            UserRepresentation user = users.get(0);
            if (user.getRequiredActions() != null && !user.getRequiredActions().isEmpty()) {
                log.debug("Clearing required actions {} for user '{}'", user.getRequiredActions(), username);
                user.setRequiredActions(List.of());
                user.setEmailVerified(true);
                usersResource.get(user.getId()).update(user);
            }
        } catch (Exception e) {
            log.warn("Failed to clear required actions for user '{}': {}", username, e.getMessage());
        }
    }
}
