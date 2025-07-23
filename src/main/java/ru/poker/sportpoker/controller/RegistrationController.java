package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.validate.UserValidator;
import ru.poker.sportpoker.validate.ValidationException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class RegistrationController {

    private final KeycloakUserService keycloakUserService;
    private final UserValidator userValidator;

    @Operation(description = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto, BindingResult bindingResult) {
        userValidator.validateRegistration(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        keycloakUserService.createUser(dto.getUsername(), dto.getEmail(), dto.getPassword(), dto.getFirstName(), dto.getLastName());
        return ResponseEntity.ok("User registered");
    }

    @Operation(description = "Вход в приложение")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto, BindingResult bindingResult) {
        userValidator.validateLogin(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            log.debug("VAL_ERROR_COUNT_LOG", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }
        AccessTokenResponse tokenResponse = keycloakUserService.authenticate(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok(tokenResponse);
    }
}