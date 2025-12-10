package ru.practicum.explorewithme.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comment.dto.CommentDetailDto;
import ru.practicum.explorewithme.comment.dto.CreateCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDetailDto createComment(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateCommentDto createCommentDto) {

        log.info("POST /events/{}/comments", eventId);
        return commentService.createComment(eventId, createCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDetailDto updateComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {

        log.info("PATCH /events/{}/comments/{}", eventId, commentId);
        return commentService.updateComment(eventId, commentId, updateCommentDto);
    }
}