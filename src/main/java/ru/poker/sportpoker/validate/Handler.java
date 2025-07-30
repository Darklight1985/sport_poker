package ru.poker.sportpoker.validate;

import lombok.Setter;
import org.springframework.validation.BindingResult;

@Setter
public abstract class Handler<T> {

    private Handler<T> nextHandler;

    public abstract void handle(BindingResult bindingResult, T... t);

    protected void getNextHandler(T request, BindingResult bindingResult) {
        if (nextHandler != null) {
            nextHandler.handle(bindingResult, request);
        }
    }
}
