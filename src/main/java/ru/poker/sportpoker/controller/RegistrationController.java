package ru.poker.sportpoker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.service.KeycloakUserService;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final KeycloakUserService keycloakUserService;

    public RegistrationController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
        try {
            keycloakUserService.createUser(dto.getUsername(), dto.getEmail(), dto.getPassword());
            return ResponseEntity.ok("User registered");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }
}