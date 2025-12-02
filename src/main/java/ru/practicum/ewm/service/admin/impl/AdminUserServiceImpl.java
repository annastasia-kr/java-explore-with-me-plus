package ru.practicum.ewm.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.admin.AdminUserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание пользователя с email: {}", newUserRequest.getEmail());

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с email=" + newUserRequest.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(newUserRequest);
        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с ID: {}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение списка пользователей: ids={}, from={}, size={}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIds(ids, pageable);
        } else {
            users = userRepository.findAll(pageable).getContent();
        }

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        userRepository.delete(user);
        log.info("Пользователь с ID {} удален", userId);
    }
}