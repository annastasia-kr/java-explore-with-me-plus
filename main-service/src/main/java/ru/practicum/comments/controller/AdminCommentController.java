package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDetailDto;
import ru.practicum.comments.dto.UpdateCommentDtoAdmin;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.service.CommentService;

import java.util.Collection;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events/{eventId}/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long eventId,
                              @PathVariable Long commentId) {

        commentService.deleteComment(eventId, commentId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<CommentDetailDto> getCommentsByAdmin(@PathVariable @Positive Long eventId,
                                                           @RequestParam(defaultValue = "0") Integer from,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @RequestParam(required = false) List<StateComment> states,
                                                           @RequestParam(required = false) List<Long> authorsIds) {

        return commentService.getCommentsByAdmin(from, size, states, authorsIds, eventId);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDetailDto getCommentById(@PathVariable @Positive Long eventId,
                                           @PathVariable @Positive Long commentId) {

        return commentService.getCommentById(commentId, eventId);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDetailDto moderateCommentById(@PathVariable @Positive Long eventId,
                                                @PathVariable @Positive Long commentId,
                                                @Valid @RequestBody UpdateCommentDtoAdmin updateCommentDtoAdmin) {

        return commentService.moderateCommentById(commentId, eventId, updateCommentDtoAdmin);
    }
}
