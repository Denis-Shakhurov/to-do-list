package org.example.userservice.mapper;

import org.example.userservice.dto.UserCreateDTO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.dto.UserUpdateDTO;
import org.example.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toEntity(UserDTO userDTO);

    UserDTO toUserDTO(User user);

    User toEntity(UserCreateDTO userCreateDTO);

    void update(UserUpdateDTO dto, @MappingTarget User user);
}