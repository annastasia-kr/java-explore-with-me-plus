package ru.practicum.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserShortDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    User toUser(NewUserRequest newUserRequest);

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    User toUser(UserShortDto dto);
}
