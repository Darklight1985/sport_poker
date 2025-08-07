package ru.poker.sportpoker.mapper;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.poker.sportpoker.dto.PlayerInfo;
import ru.poker.sportpoker.dto.PlayerShortInfo;
import ru.poker.sportpoker.dto.UserView;

import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "userId", source = "id")
    PlayerInfo getUserInfo(UserRepresentation userRepresentation);

    PlayerShortInfo getUserShortInfo(PlayerInfo playerInfo);

    Set<PlayerShortInfo> getUserShortInfoList(Collection<PlayerInfo> playerInfos);

    @Mapping(target = "userId", source = "id")
    UserView getUserView(UserRepresentation userRepresentation);
}
