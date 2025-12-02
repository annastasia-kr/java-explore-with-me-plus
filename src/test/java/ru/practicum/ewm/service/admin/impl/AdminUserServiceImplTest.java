package ru.practicum.ewm.service.admin.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User user;
    private UserDto userDto;
    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .createdDate(LocalDateTime.now())
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .createdDate(LocalDateTime.now())
                .build();

        newUserRequest = NewUserRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(NewUserRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = adminUserService.createUser(newUserRequest);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmail(newUserRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowConflictException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> adminUserService.createUser(newUserRequest));

        assertEquals("Пользователь с email=john.doe@example.com уже существует", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsers_WithoutIds_ShouldReturnAllUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(userRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        List<UserDto> result = adminUserService.getUsers(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.get(0).getId());

        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getUsers_WithIds_ShouldReturnFilteredUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(userRepository.findAllByIds(anyList(), eq(pageRequest))).thenReturn(List.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        List<UserDto> result = adminUserService.getUsers(List.of(1L, 2L), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findAllByIds(anyList(), eq(pageRequest));
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        adminUserService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> adminUserService.deleteUser(999L));

        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }
}