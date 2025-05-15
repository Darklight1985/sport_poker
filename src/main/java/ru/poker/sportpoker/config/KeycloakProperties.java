package ru.poker.sportpoker.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String authServerUrl;

    private String realm;

    private String resource;

    private String resourceUser;

    private Credentials credentials;

    @Setter
    @Getter
    public static class Credentials {
        private String secret;

        private String userSecret;
    }
}
