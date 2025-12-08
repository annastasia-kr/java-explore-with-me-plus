package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentService;

import java.time.LocalDateTime;
import java.util.Collection;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/events/{eventId}/comments")
public class PublicCommentController {

    CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<CommentDto> getEventCommentsPublic(@PathVariable Long eventId,
                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
                                                         @RequestParam(required = false, defaultValue = "0") Integer from,
                                                         @RequestParam(required = false, defaultValue = "10") Integer size) {
        return commentService.getEventCommentsPublic(eventId, created, from, size);
    }
}
