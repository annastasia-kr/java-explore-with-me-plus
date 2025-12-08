package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdateCommentDtoAdminRequest;
import ru.practicum.comments.dto.UpdateCommentDtoUserRequest;
import ru.practicum.comments.enums.StateComment;

import java.time.LocalDateTime;
import java.util.Collection;

public interface CommentService {
    Collection<CommentDto> getCommentsByAdmin(Integer from, Integer size, StateComment state);

    Collection<CommentDto> getCommentsByEventId(Integer from, Integer size, Long eventId);

    CommentDto moderateCommentById(Long commentId, UpdateCommentDtoAdminRequest updateCommentDtoAdminRequest);

    Collection<CommentDto> getCommentsByUserId(Long userId, Integer from, Integer size);

    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentByUser(Long userId, Long commentId, UpdateCommentDtoUserRequest updateCommentDtoUserRequest);

    void deleteComment(Long userId, Long commentId);

    Collection<CommentDto> getEventCommentsPublic(Long eventId, LocalDateTime created, Integer from, Integer size);
}
