package ru.practicum.explorewithme.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comment.dto.CommentDetailDto;
import ru.practicum.explorewithme.comment.dto.UpdateAdminCommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events/{eventId}/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {

        log.info("DELETE /admin/events/{}/comments/{}", eventId, commentId);
        commentService.deleteComment(eventId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDetailDto getComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId) {

        log.info("GET /admin/events/{}/comments/{}", eventId, commentId);
        return commentService.getComment(eventId, commentId);
    }

    @GetMapping
    public List<CommentDetailDto> getComments(
            @PathVariable Long eventId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /admin/events/{}/comments", eventId);
        return commentService.getComments(eventId, state, authorId, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDetailDto moderateComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateAdminCommentDto updateAdminCommentDto) {

        log.info("PATCH /admin/events/{}/comments/{}", eventId, commentId);
        return commentService.moderateComment(eventId, commentId, updateAdminCommentDto);
    }
}