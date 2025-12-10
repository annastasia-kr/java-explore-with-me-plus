package ru.practicum.explorewithme.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentDetailDto;
import ru.practicum.explorewithme.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "state", ignore = true)
    CommentDto toDto(Comment comment);

    @Mapping(target = "authorId", source = "author.id")
    CommentDetailDto toDetailDto(Comment comment);
}