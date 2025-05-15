package ru.poker.sportpoker.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.poker.sportpoker.exception.UserRegistrationException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<String> handleBadRequest(UserRegistrationException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}