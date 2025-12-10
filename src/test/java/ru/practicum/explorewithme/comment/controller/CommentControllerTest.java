package ru.practicum.explorewithme.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.comment.dto.CommentDetailDto;
import ru.practicum.explorewithme.comment.dto.CreateCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private CommentDetailDto commentDetailDto;
    private CreateCommentDto createCommentDto;
    private UpdateCommentDto updateCommentDto;

    CommentControllerTest(CommentService commentService) {
        this.commentService = commentService;
    }

    @BeforeEach
    void setUp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        commentDetailDto = new CommentDetailDto(
                1L,
                "Test comment",
                1L,
                "SUBMITTED",
                LocalDateTime.now(),
                null
        );

        createCommentDto = new CreateCommentDto("Test comment", 1L);
        updateCommentDto = new UpdateCommentDto("Updated comment", 1L);
    }

    @Test
    void createComment_whenValidData_thenReturnCreated() throws Exception {
        when(commentService.createComment(anyLong(), any(CreateCommentDto.class)))
                .thenReturn(commentDetailDto);

        mockMvc.perform(post("/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.authorId").value(1L))
                .andExpect(jsonPath("$.state").value("SUBMITTED"));
    }

    @Test
    void createComment_whenMissingText_thenReturnBadRequest() throws Exception {
        CreateCommentDto invalidDto = new CreateCommentDto("", 1L);

        mockMvc.perform(post("/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenMissingAuthorId_thenReturnBadRequest() throws Exception {
        CreateCommentDto invalidDto = new CreateCommentDto("Test comment", null);

        mockMvc.perform(post("/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_whenValidData_thenReturnOk() throws Exception {
        CommentDetailDto updatedDto = new CommentDetailDto(
                1L,
                "Updated comment",
                1L,
                "SUBMITTED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(commentService.updateComment(anyLong(), anyLong(), any(UpdateCommentDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/events/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    @Test
    void updateComment_whenMissingText_thenReturnBadRequest() throws Exception {
        UpdateCommentDto invalidDto = new UpdateCommentDto("", 1L);

        mockMvc.perform(patch("/events/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}