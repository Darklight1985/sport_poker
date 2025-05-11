package ru.poker.sportpoker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfig {

    @Value("${keycloak.resource}")
    private String clientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/*").permitAll()
                        .requestMatchers("/api/offline/**").hasRole("user")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) // Настройка обработки JWT
                );
        System.out.println("SecurityFilterChain created!");
        return http.build();

    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000") // ваш фронт
                        .allowedMethods("*");
            }
        };
    }

    /**
     * Настройка конвертера для обработки ролей из токена Keycloak.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> claims = jwt.getClaims();
            if (claims.containsKey("resource_access")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) claims.get("resource_access");
                if (realmAccess.containsKey("pokerClient")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> clientAccess = (Map<String, Object>) realmAccess.get(clientId);
                    List<String> roles = (List<String>) clientAccess.get("roles");
                    log.info("extracted roles: " + roles);
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
            return authorities;
        });
        return converter;
    }
}