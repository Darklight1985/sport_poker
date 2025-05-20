package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.service.KeycloakUserService;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final KeycloakUserService keycloakUserService;

    public RegistrationController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    @Operation(description = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
        try {
            keycloakUserService.createUser(dto.getUsername(), dto.getEmail(), dto.getPassword());
            return ResponseEntity.ok("User registered");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    @Operation(description = "Вход в приложение")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Parameter(description = "Никнейм пользователя") @RequestParam String username,
                                   @Parameter(description = "Пароль пользователя") @RequestParam String password) {
        try {
            AccessTokenResponse tokenResponse = keycloakUserService.authenticate(username, password);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}