package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.dto.UploadFileResponse;
import ru.poker.sportpoker.dto.UserLoginDto;
import ru.poker.sportpoker.dto.UserRegistrationDto;
import ru.poker.sportpoker.dto.UserView;
import ru.poker.sportpoker.service.KeycloakUserService;
import ru.poker.sportpoker.service.UserService;
import ru.poker.sportpoker.validate.ValidationException;
import ru.poker.sportpoker.validate.user.UserValidator;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class UsersController {

    private final KeycloakUserService keycloakUserService;
    private final UserValidator userValidator;
    private final UserService userService;

    @Operation(description = "Получение информации о текущем пользователе")
    @GetMapping
    public UserView getUser() {
        return userService.getUser();
    }

    @Operation(description = "Получение аватара пользователя")
    @GetMapping("{id}/avatar")
    public ResponseEntity<InputStreamResource> getAvatar(@PathVariable UUID id) {
        return new ResponseEntity<>(null, getHeaders(
                MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE)), HttpStatus.OK);
    }

    @Operation(description = "Привязка аватара пользователю")
    @PostMapping(value = {"/{id}/add-avatar"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadFileResponse uploadEmployeeAvatar(@PathVariable UUID id,
                                            @RequestParam("avatar") MultipartFile file) throws IOException {
        return userService.uploadAvatar(id, file);
    }

    private HttpHeaders getHeaders(MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        return headers;
    }
}