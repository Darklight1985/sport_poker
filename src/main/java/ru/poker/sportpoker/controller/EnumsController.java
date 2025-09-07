package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.poker.sportpoker.dto.EnumDto;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.mapper.EnuMapper;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/enums")
public class EnumsController {

    private final EnuMapper enuMapper;

    @Operation(description = "Получение списка упражнений")
    @GetMapping("/exercises")
    public List<EnumDto> getExercises () {
        return enuMapper.getExercises(Arrays.stream(Exercises.values()).toList());
    }
}
