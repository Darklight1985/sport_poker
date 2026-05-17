package ru.poker.sportpoker.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.poker.sportpoker.dto.EnumDto;
import ru.poker.sportpoker.enums.Cards;
import ru.poker.sportpoker.enums.Exercises;
import ru.poker.sportpoker.enums.Suits;
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

    @Operation(description = "Получение списка упражнений")
    @GetMapping("/exercises")
    public List<EnumDto> getCards () {
        return enuMapper.getCards(Arrays.stream(Cards.values()).toList()); 
    }

    @Operation(description = "Получение списка упражнений")
    @GetMapping("/exercises")
    public List<EnumDto> getSuits () {
        return enuMapper.getSuits(Arrays.stream(Suits.values()).toList());
    }

    @Operation(description = "Получение изображения карты")
    @GetMapping("/cards/{suit}/{card}")
    public ResponseEntity<InputStreamResource> getCard (@PathVariable String suit, String card) {
        return null;
    }
}
