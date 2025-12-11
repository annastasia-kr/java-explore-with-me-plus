package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDetailDto;
import ru.practicum.comments.dto.CreateCommentDto;
import ru.practicum.comments.dto.UpdateCommentDto;
import ru.practicum.comments.service.CommentService;


@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/events/{eventId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDetailDto createComment(@PathVariable Long eventId,
                                          @RequestBody @NotNull @Valid CreateCommentDto createCommentDto) {
        return commentService.createComment(eventId, createCommentDto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDetailDto updateCommentByUser(@PathVariable Long eventId,
                                                @PathVariable Long commentId,
                                                @RequestBody @NotNull @Valid UpdateCommentDto updateCommentDto) {
        return commentService.updateCommentByUser(eventId, commentId, updateCommentDto);
    }

}
