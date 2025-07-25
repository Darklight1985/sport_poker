package ru.poker.sportpoker.validate.room;

import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.validate.Handler;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

abstract class RoomHandler<T> extends Handler<T> {

    @Override
    public void handle(BindingResult bindingResult, T... request) {
        if (request == null || request.length == 0) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Аргументы для валидации не заданы");
            return;
        }
        var arg = request[0];
        if (arg == null) {
            bindingResult.reject(ErrorCodes.FIELD_IS_NULL, "Задайте ДТО");
            return;
        }

        handleSpecifics(bindingResult, request);
        getNextHandler(arg, bindingResult);
    }

    protected abstract void handleSpecifics(BindingResult bindingResult, T... request);
}
