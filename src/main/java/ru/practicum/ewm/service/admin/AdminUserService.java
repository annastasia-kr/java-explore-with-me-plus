package ru.practicum.ewm.service.admin;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import java.util.List;

public interface AdminUserService {
    UserDto createUser(NewUserRequest newUserRequest);
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);
    void deleteUser(Long userId);
}