package ru.practicum.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.comments.enums.StateComment;

import java.time.LocalDateTime;

public class CommentDetailDto {

    private Long id;

    private String text;

    private Long eventId;

    private Long authorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEdited;

    StateComment state;
}
