package ru.poker.sportpoker.validate.room;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.validate.errors.ErrorCodes;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExercisesHandler extends RoomHandler<Set<Exercises>> {

    @Override
    protected void handleSpecifics(BindingResult bindingResult, Set<Exercises>... args) {
        if (args == null || args.length == 0) {
            bindingResult.rejectValue(ErrorCodes.FIELD_IS_NULL, "Список упражнений должен быть задан");
            return;
        }
        if (bindingResult.hasErrors()) {
            return;
        }

        Set<Exercises> exercises = args[0];

        if (exercises.size() != 4) {
            bindingResult.reject(ErrorCodes.VALUE_CONSTRAINT_VIOLATION, "Количество упражнений должно быть равно четырем");
        }
    }
}
