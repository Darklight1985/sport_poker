package ru.poker.sportpoker.mapper;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.poker.sportpoker.dto.UserInfo;
import ru.poker.sportpoker.dto.UserShortInfo;

import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "userId", source = "id")
    UserInfo getUserInfo(UserRepresentation userRepresentation);

    UserShortInfo getUserShortInfo(UserInfo userInfo);

    Set<UserShortInfo> getUserShortInfoList(Collection<UserInfo> userInfos);
}
