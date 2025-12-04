package ru.practicum.requests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainServiceApplication;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dto.RequestDto;
import ru.practicum.requests.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivateRequestController.class)
@ContextConfiguration(classes = {MainServiceApplication.class})
class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    private RequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new RequestDto();
        requestDto.setId(1L);
        requestDto.setEvent(10L);
        requestDto.setRequester(2L);
        requestDto.setStatus("PENDING");
        requestDto.setCreated(LocalDateTime.now());
    }

    @Test
    void getUserRequests_ShouldReturnListOfRequests() throws Exception {
        when(requestService.getUserRequests(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/requests", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestDto.getId()))
                .andExpect(jsonPath("$[0].event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$[0].requester").value(requestDto.getRequester()))
                .andExpect(jsonPath("$[0].status").value(requestDto.getStatus()));

        verify(requestService, times(1)).getUserRequests(1L);
    }

    @Test
    void getUserRequests_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        when(requestService.getUserRequests(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/users/{userId}/requests", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));

        verify(requestService, times(1)).getUserRequests(999L);
    }

    @Test
    void createParticipationRequest_ShouldReturnCreatedRequest() throws Exception {
        when(requestService.create(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$.requester").value(requestDto.getRequester()));

        verify(requestService, times(1)).create(1L, 10L);
    }

    @Test
    void createParticipationRequest_WithInvalidEventId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "0"))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).create(anyLong(), anyLong());
    }

    @Test
    void createParticipationRequest_WithInvalidEventId_Negative_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "-1"))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).create(anyLong(), anyLong());
    }

    @Test
    void createParticipationRequest_WithConflict_ShouldReturnConflict() throws Exception {
        when(requestService.create(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Инициатор события не может подать заявку на участие"));

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        verify(requestService, times(1)).create(1L, 10L);
    }

    @Test
    void cancelRequest_ShouldReturnOk() throws Exception {
        when(requestService.cancelRequest(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.status").value(requestDto.getStatus()));

        verify(requestService, times(1)).cancelRequest(1L, 1L);
    }

    @Test
    void cancelRequest_WithNonExistentRequest_ShouldReturnNotFound() throws Exception {
        when(requestService.cancelRequest(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Запрос на участие с id=999 не найден"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));

        verify(requestService, times(1)).cancelRequest(1L, 999L);
    }

    @Test
    void cancelRequest_WithConflict_ShouldReturnConflict() throws Exception {
        when(requestService.cancelRequest(anyLong(), anyLong()))
                .thenThrow(new ConflictException("Запрос не принадлежит пользователю"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 2L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        verify(requestService, times(1)).cancelRequest(1L, 2L);
    }

    @Test
    void createParticipationRequest_WithoutEventId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 1L))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).create(anyLong(), anyLong());
    }
}