package ru.poker.sportpoker.mapper;

import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.poker.sportpoker.dto.UserInfo;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserInfo getUserInfo(UserRepresentation userRepresentation);
}
