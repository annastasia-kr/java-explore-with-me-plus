package ru.practicum.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainServiceApplication;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@ContextConfiguration(classes = { MainServiceApplication.class })
class AdminUserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

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
                when(userService.createUser(any(NewUserRequest.class))).thenReturn(userDto);

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(userDto.getId()))
                                .andExpect(jsonPath("$.name").value(userDto.getName()))
                                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

                verify(userService, times(1)).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
                newUserRequest.setEmail("invalid-email");

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithEmptyName_ShouldReturnBadRequest() throws Exception {
                newUserRequest.setName("");

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithTooShortName_ShouldReturnBadRequest() throws Exception {
                newUserRequest.setName("A");

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithTooLongName_ShouldReturnBadRequest() throws Exception {
                newUserRequest.setName("A".repeat(251));

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
                newUserRequest.setEmail("");

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
                when(userService.createUser(any(NewUserRequest.class)))
                                .thenThrow(new ConflictException(
                                                "Пользователь с email=john.doe@example.com уже существует"));

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newUserRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value("CONFLICT"));

                verify(userService, times(1)).createUser(any(NewUserRequest.class));
        }

        @Test
        void getUsers_ShouldReturnListOfUsers() throws Exception {
                when(userService.getUsers(anyInt(), anyInt())).thenReturn(List.of(userDto));

                mockMvc.perform(get("/admin/users")
                                .param("from", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                                .andExpect(jsonPath("$[0].name").value(userDto.getName()))
                                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()));

                verify(userService, times(1)).getUsers(anyInt(), anyInt());
        }

        @Test
        void getUsers_WithIds_ShouldReturnFilteredUsers() throws Exception {
                when(userService.getUsers(anyList(), anyInt(), anyInt())).thenReturn(List.of(userDto));

                mockMvc.perform(get("/admin/users")
                                .param("ids", "1,2,3")
                                .param("from", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(userDto.getId()));

                verify(userService, times(1)).getUsers(anyList(), anyInt(), anyInt());
        }

        @Test
        void getUsers_WithEmptyIdsList_ShouldReturnAllUsers() throws Exception {
                when(userService.getUsers(anyInt(), anyInt())).thenReturn(List.of(userDto));

                mockMvc.perform(get("/admin/users")
                                .param("ids", "")
                                .param("from", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk());

                verify(userService, times(1)).getUsers(anyInt(), anyInt());
        }

        @Test
        void getUsers_WithInvalidFrom_ShouldReturnBadRequest() throws Exception {
                mockMvc.perform(get("/admin/users")
                                .param("from", "-1")
                                .param("size", "10"))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).getUsers(anyList(), anyInt(), anyInt());
        }

        @Test
        void getUsers_WithInvalidSize_ShouldReturnBadRequest() throws Exception {
                mockMvc.perform(get("/admin/users")
                                .param("from", "0")
                                .param("size", "0"))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).getUsers(anyList(), anyInt(), anyInt());
        }

        @Test
        void getUsers_WithNegativeSize_ShouldReturnBadRequest() throws Exception {
                mockMvc.perform(get("/admin/users")
                                .param("from", "0")
                                .param("size", "-1"))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).getUsers(anyList(), anyInt(), anyInt());
        }

        @Test
        void getUsers_WithoutPaginationParams_ShouldUseDefaults() throws Exception {
                when(userService.getUsers(anyInt(), anyInt())).thenReturn(List.of(userDto));

                mockMvc.perform(get("/admin/users"))
                                .andExpect(status().isOk());

                verify(userService, times(1)).getUsers(0, 10);
        }

        @Test
        void deleteUser_ShouldReturnNoContent() throws Exception {
                doNothing().when(userService).deleteUser(anyLong());

                mockMvc.perform(delete("/admin/users/{userId}", 1L))
                                .andExpect(status().isNoContent());

                verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
                doThrow(new NotFoundException("Пользователь с id=999 не найден"))
                                .when(userService).deleteUser(anyLong());

                mockMvc.perform(delete("/admin/users/{userId}", 999L))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value("NOT_FOUND"));

                verify(userService, times(1)).deleteUser(999L);
        }

        @Test
        void deleteUser_WithZeroId_ShouldReturnNotFound() throws Exception {
                doThrow(new NotFoundException("Пользователь с id=0 не найден"))
                                .when(userService).deleteUser(anyLong());

                mockMvc.perform(delete("/admin/users/{userId}", 0L))
                                .andExpect(status().isNotFound());

                verify(userService, times(1)).deleteUser(0L);
        }

        @Test
        void createUser_WithValidMinimalData_ShouldReturnCreated() throws Exception {
                NewUserRequest minimalRequest = NewUserRequest.builder()
                                .name("Ab")
                                .email("ab@cd.ef")
                                .build();

                UserDto minimalUserDto = UserDto.builder()
                                .id(2L)
                                .name("Ab")
                                .email("ab@cd.ef")
                                .createdDate(LocalDateTime.now())
                                .build();

                when(userService.createUser(any(NewUserRequest.class))).thenReturn(minimalUserDto);

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(minimalRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(minimalUserDto.getId()))
                                .andExpect(jsonPath("$.name").value("Ab"))
                                .andExpect(jsonPath("$.email").value("ab@cd.ef"));

                verify(userService, times(1)).createUser(any(NewUserRequest.class));
        }

        @Test
        void createUser_WithValidMaximalData_ShouldReturnCreated() throws Exception {
                NewUserRequest maximalRequest = NewUserRequest.builder()
                                .name("A".repeat(250))
                                .email("test@example.com") // Корректный email
                                .build();

                UserDto maximalUserDto = UserDto.builder()
                                .id(3L)
                                .name("A".repeat(250))
                                .email("test@example.com")
                                .createdDate(LocalDateTime.now())
                                .build();

                when(userService.createUser(any(NewUserRequest.class))).thenReturn(maximalUserDto);

                mockMvc.perform(post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(maximalRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(maximalUserDto.getId()));

                verify(userService, times(1)).createUser(any(NewUserRequest.class));
        }

        @Test
        void getUsers_WithFromGreaterThanZero_ShouldWork() throws Exception {
                when(userService.getUsers(anyInt(), anyInt())).thenReturn(List.of());

                mockMvc.perform(get("/admin/users")
                                .param("from", "10")
                                .param("size", "5"))
                                .andExpect(status().isOk());

                verify(userService, times(1)).getUsers(10, 5);
        }
}