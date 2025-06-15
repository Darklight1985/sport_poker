package ru.poker.sportpoker.mapper;

import org.mapstruct.*;
import ru.poker.sportpoker.domain.GameRoom;
import ru.poker.sportpoker.dto.GameRoomView;
import ru.poker.sportpoker.dto.UserShortInfo;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "players", source = "players")
    GameRoomView getView(GameRoom room, UserShortInfo creator, Set<UserShortInfo> players);

    @AfterMapping
    default void setMinutesLeft(@MappingTarget GameRoomView view, GameRoom room) {
        if (room.getCountDownTimer() != null) {
            view.setMinutesLeft(room.getCountDownTimer().getMinutesLeft());
        }
    }
}
