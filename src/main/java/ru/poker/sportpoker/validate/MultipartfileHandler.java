package ru.poker.sportpoker.validate;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public  class MultipartfileHandler extends Handler<MultipartFile> {

    private static final long MAX_SIZE_AVATAR = 8388608L;
    private final Pattern pattern = Pattern.compile("([^\s]+(\\.(?i)(jpe?g|png|gif|bmp))$)");

    @Override
    public void handle(BindingResult bindingResult, MultipartFile... request) {
        if (request == null || request.length == 0) {
            bindingResult.reject(ErrorCodes.OBJECT_IS_NULL, "Аргументы для валидации не заданы");
            return;
        }

        var arg = request[0];
        if (arg == null) {
            bindingResult.reject("Dto is null", "Задайте ДТО");
            return;
        }

        handleSpecifics(bindingResult, request);
        getNextHandler(arg, bindingResult);
    }

    public void handleSpecifics(BindingResult bindingResult, MultipartFile... request) {
        var file = request[0];

        if (file.getSize() > MAX_SIZE_AVATAR) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Максимальный размер фото не более 8 Мб");
        }
        Matcher matcher = pattern.matcher(Objects.requireNonNull(file.getOriginalFilename()));
        if (!matcher.find()) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Не подходящий формат аватара");
        }
    }
}
