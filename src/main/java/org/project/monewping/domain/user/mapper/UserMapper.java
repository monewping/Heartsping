package org.project.monewping.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.LoginResponse;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    User toEntity(UserRegisterRequest request);

    UserRegisterResponse toResponse(User user);

    LoginResponse toLoginResponse(User user);
}