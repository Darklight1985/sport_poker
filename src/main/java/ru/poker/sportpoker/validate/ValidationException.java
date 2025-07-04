package ru.poker.sportpoker.validate;

import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Ошибка валидации. Соответствует {@link org.springframework.http.HttpStatus#UNPROCESSABLE_ENTITY}
 * <p>
 * Содержит объект {@link BindingResult}, на основе которого строится сообщение в методе {@link #getMessage()}
 */
public class ValidationException extends RuntimeException {

    private static final String ERROR_COUNT_TEMPLATE = " with %s errors: ";
    private static final String VALIDATION_ERROR_MES_TEMPLATE = "[%s : %s]";

    @Getter
    private final transient BindingResult bindingResult;

    public ValidationException(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public ValidationException(BindingResult bindingResult, String message) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public ValidationException(BindingResult bindingResult, String message, Throwable cause) {
        super(message, cause);
        this.bindingResult = bindingResult;
    }

    public ValidationException(BindingResult bindingResult, Throwable cause) {
        super(cause);
        this.bindingResult = bindingResult;
    }

    public ValidationException(BindingResult bindingResult, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bindingResult = bindingResult;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Validation failed");
        if (bindingResult == null) {
            return sb.toString();
        }
        if (bindingResult.getErrorCount() > 0) {
            sb.append(ERROR_COUNT_TEMPLATE.formatted(bindingResult.getErrorCount()));
            for (ObjectError error : bindingResult.getAllErrors()) {
                sb.append(VALIDATION_ERROR_MES_TEMPLATE.formatted(error.getCode(), error.getDefaultMessage()));
            }
        }
        return sb.toString();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
