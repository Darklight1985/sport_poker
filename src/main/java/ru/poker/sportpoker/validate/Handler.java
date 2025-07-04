package ru.poker.sportpoker.validate;

import lombok.Setter;
import org.springframework.validation.BindingResult;

@Setter
abstract class Handler<T> {

    private Handler<T> nextHandler;

    public abstract void handle(T t, BindingResult bindingResult);

    protected void getNextHandler(T request, BindingResult bindingResult) {
        if (nextHandler != null) {
            nextHandler.handle(request, bindingResult);
        }
    }
}
