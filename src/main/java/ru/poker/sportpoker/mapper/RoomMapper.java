package ru.poker.sportpoker.mapper;

import org.mapstruct.*;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.CreateGameRoomDto;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.UpdateGameRoomDto;
import ru.poker.sportpoker.dto.UserShortInfo;

import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "players", source = "players")
    GameRoomView getView(GameRoom room, UserShortInfo creator, Set<UserShortInfo> players);

    @Mapping(target = "creator", source = "userId")
    GameRoom toGameRoom(CreateGameRoomDto dto, UUID userId);

    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "gameTime", source = "dto.gameTime")
    void updateGameRoom(@MappingTarget GameRoom gameRoom, UpdateGameRoomDto dto);

    @AfterMapping
    default void setMinutesLeft(@MappingTarget GameRoomView view, GameRoom room) {
        if (room.getCountDownTimer() != null) {
            view.setMinutesLeft(room.getCountDownTimer().getMinutesLeft());
        }
    }
}
