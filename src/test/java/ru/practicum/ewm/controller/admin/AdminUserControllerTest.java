package ru.practicum.ewm.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.service.admin.AdminUserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    private UserDto userDto;
    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
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
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(adminUserService.createUser(any(NewUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(adminUserService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        newUserRequest.setEmail("invalid-email");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).createUser(any(NewUserRequest.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        when(adminUserService.createUser(any(NewUserRequest.class)))
                .thenThrow(new ConflictException("Пользователь с email=john.doe@example.com уже существует"));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        verify(adminUserService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    void getUsers_ShouldReturnListOfUsers() throws Exception {
        when(adminUserService.getUsers(anyList(), anyInt(), anyInt())).thenReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()))
                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()));

        verify(adminUserService, times(1)).getUsers(anyList(), anyInt(), anyInt());
    }

    @Test
    void getUsers_WithIds_ShouldReturnFilteredUsers() throws Exception {
        when(adminUserService.getUsers(anyList(), anyInt(), anyInt())).thenReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1,2,3")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()));

        verify(adminUserService, times(1)).getUsers(anyList(), anyInt(), anyInt());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(adminUserService).deleteUser(anyLong());

        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(adminUserService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("Пользователь с id=999 не найден"))
                .when(adminUserService).deleteUser(anyLong());

        mockMvc.perform(delete("/admin/users/{userId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));

        verify(adminUserService, times(1)).deleteUser(999L);
    }
}