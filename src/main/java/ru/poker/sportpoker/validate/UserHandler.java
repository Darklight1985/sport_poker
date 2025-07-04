package ru.poker.sportpoker.validate;


import org.springframework.validation.BindingResult;

abstract class UserHandler<T> extends Handler<T> {

    @Override
    public void handle(T request, BindingResult bindingResult) {
        if (request == null) {
            bindingResult.reject("Dto is null", "Задайте ДТО");
            return;
        }

        handleSpecifics(request, bindingResult);
        getNextHandler(request, bindingResult);
    }

    protected abstract void handleSpecifics(T request, BindingResult bindingResult);
}
