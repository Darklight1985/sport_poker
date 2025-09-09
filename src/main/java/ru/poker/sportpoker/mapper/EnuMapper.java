package ru.poker.sportpoker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.poker.sportpoker.dto.EnumDto;
import ru.poker.sportpoker.enums.Exercises;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnuMapper {

    @Mapping(target = "name", expression = "java(exercises.name())")
    EnumDto getExercise (Exercises exercises);

    List<EnumDto> getExercises (List<Exercises> exercises);
}
