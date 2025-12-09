package ru.practicum.comments.service;

import ru.practicum.comments.dto.*;
import ru.practicum.comments.enums.StateComment;

import java.util.Collection;
import java.util.List;

public interface CommentService {
    Collection<CommentDetailDto> getCommentsByAdmin(Integer from, Integer size, List<StateComment> state, List<Long> authorsIds, Long eventId);

    CommentDetailDto moderateCommentById(Long commentId, Long eventId, UpdateCommentDtoAdmin updateCommentDtoAdmin);

    CommentDetailDto createComment(Long eventId, CreateCommentDto createCommentDto);

    CommentDetailDto updateCommentByUser(Long eventId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteComment();

    CommentDetailDto getCommentById(Long commentId, Long eventId);
}
