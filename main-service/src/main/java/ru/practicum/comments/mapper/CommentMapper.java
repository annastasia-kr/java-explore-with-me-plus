package ru.practicum.comments.mapper;

import org.mapstruct.*;
import ru.practicum.comments.dto.CommentDetailDto;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CreateCommentDto;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.model.Comment;
import ru.practicum.events.model.Event;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        imports = {LocalDateTime.class, StateComment.class})
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "createCommentDto.text", target = "text")
    @Mapping(source = "author", target = "author")
    @Mapping(source = "event", target = "event")
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    @Mapping(target = "state", constant = "SUBMITTED")
    Comment toComment(CreateCommentDto createCommentDto, Event event, User author);

    @Mapping(source = "text", target = "text")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "lastEdited", target = "lastEdited")
    CommentDto toCommentDto(Comment comment);

    @Mapping(source = "text", target = "text")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "created", target = "created")
    @Mapping(source = "lastEdited", target = "lastEdited")
    @Mapping(source = "state", target = "state")
    CommentDetailDto toCommentDetailDto(Comment comment);
}
