package ru.poker.sportpoker.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.poker.sportpoker.exception.UserRegistrationException;

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
                .grantType("client_credentials")
                .build();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
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
}
