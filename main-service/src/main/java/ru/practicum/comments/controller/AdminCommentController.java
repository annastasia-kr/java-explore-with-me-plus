package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.UpdateCommentDtoAdminRequest;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.service.CommentService;

import java.util.Collection;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<CommentDto> getCommentsByAdmin(@RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam(required = false) StateComment state) {

        return commentService.getCommentsByAdmin(from, size, state);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<CommentDto> getCommentsByEventId(@RequestParam(defaultValue = "0") Integer from,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam @Positive Long eventId) {

        return commentService.getCommentsByEventId(from, size, eventId);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
        public CommentDto moderateCommentById(@PathVariable @Positive Long commentId,
                                              @Valid @RequestBody UpdateCommentDtoAdminRequest updateCommentDtoAdminRequest) {

            return commentService.moderateCommentById(commentId, updateCommentDtoAdminRequest);
        }
}
