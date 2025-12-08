package ru.practicum.comments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.model.Comment;
import ru.practicum.events.model.Event;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        imports = {LocalDateTime.class, StateComment.class}, uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "text", target = "text")
    @Mapping(source = "author", target = "author")
    @Mapping(source = "event", target = "event")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "lastEdited", constant = "null")
    @Mapping(target = "state", constant = "SUBMITTED")
    Comment toComment(NewCommentDto newCommentDto, User author, Event event);

    @Mapping(source = "text", target = "text")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "author", target = "author")
    @Mapping(source = "createdOn", target = "createdOn")
    @Mapping(source = "lastEdited", target = "lastEdited")
    CommentDto toCommentDto(Comment comment);
}
