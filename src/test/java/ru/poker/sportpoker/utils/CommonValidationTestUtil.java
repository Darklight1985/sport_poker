package ru.poker.sportpoker.utils;


import io.micrometer.common.lang.NonNull;
import org.junit.jupiter.api.Assertions;
import org.springframework.validation.BindingResult;

/**
 * Общие вспомогательные методы для тестов валидаторов.
 */
public class CommonValidationTestUtil {

    /**
     * Проверяет, что все ошибки внутри заданного {@link BindingResult} имеют заданный код ошибки.
     *
     * @param errorCode     Код ошибки.
     * @param bindingResult Проверяемое хранилище ошибок.
     */
    public static void assertErrorCodeEquals(String errorCode, @NonNull BindingResult bindingResult) {
        bindingResult.getAllErrors().forEach(error -> Assertions.assertEquals(errorCode, error.getCode()));
    }
}
