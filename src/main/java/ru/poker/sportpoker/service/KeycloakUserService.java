package ru.poker.sportpoker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.exception.UserRegistrationException;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Service
public class KeycloakUserService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public void createUser(String username, String email, String password) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName("Dmitrij");
        user.setLastName("Kolesnev");
        user.setEnabled(true);

        UsersResource userResource = keycloak.realm(realm).users();

        try {
            Response response = userResource.create(user);
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

                keycloak.realm(realm).users().get(userId).resetPassword(passwordCred);
            }
        } catch (WebApplicationException ex) {
            String error = ex.getResponse().readEntity(String.class);
            throw new UserRegistrationException("Ошибка Keycloak: " + error);
        }
    }


    public void getCurrentUser() {
        // Получение текущей аутентификации
        Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();

        JwtAuthenticationToken tokenA = (JwtAuthenticationToken) authentication;
        // Получение Principal объекта, содержащего JWT
        Jwt principal = (Jwt) authentication.getPrincipal();

        // Извлечение JWT токена
        String token = principal.getTokenValue();
        var atr = tokenA.getTokenAttributes();


        // Получение идентификатора пользователя из claims
        String userId = (String) atr.get("sub");
        System.out.println("User ID: " + userId);
    }

    /**
     * Аутентификация пользователя через Keycloak.
     */
    public AccessTokenResponse authenticate(String username, String password) {
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId("userPokerClient")
                    .clientSecret("3OMZrVJ4D5XB9mncxayNL2O2l51BKHCO")
                    .grantType(OAuth2Constants.PASSWORD)
                    .username(username)
                    .password(password)
                    .build();

            return keycloak.tokenManager().getAccessToken();
        } catch (Exception e) {
            e.printStackTrace(); // Выведет подробности
            throw new RuntimeException("Invalid username or password", e);
        }
    }
}
