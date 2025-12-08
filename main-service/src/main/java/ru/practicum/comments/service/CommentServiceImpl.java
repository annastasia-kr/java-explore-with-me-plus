package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdateCommentDtoAdminRequest;
import ru.practicum.comments.dto.UpdateCommentDtoUserRequest;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<CommentDto> getCommentsByAdmin(Integer from, Integer size, StateComment state) {
        Pageable page = PageRequest.of(from / size, size);

        return commentRepository.findAllByState(page, state).stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    public Collection<CommentDto> getCommentsByEventId(Integer from, Integer size, Long eventId) {
        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        Pageable page = PageRequest.of(from / size, size);
        return commentRepository.findAllByEventId(eventId, page).stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto moderateCommentById(Long commentId, UpdateCommentDtoAdminRequest updateCommentDtoAdminRequest) {
        Comment findedComment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found"));

        if(updateCommentDtoAdminRequest.getState() != null) {
            findedComment.setState(updateCommentDtoAdminRequest.getState());
        }
        return commentMapper.toCommentDto(commentRepository.save(findedComment));
    }

    @Override
    public Collection<CommentDto> getCommentsByUserId(Long userId, Integer from, Integer size) {
        return null;
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User foundedUser = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));
        Event foundedEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        Comment createdComment = commentMapper.toComment(newCommentDto, foundedUser, foundedEvent);

        return commentMapper.toCommentDto(commentRepository.save(createdComment));
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(Long userId, Long commentId, UpdateCommentDtoUserRequest updateCommentDtoUserRequest) {
        return null;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {

    }

    @Override
    public Collection<CommentDto> getEventCommentsPublic(Long eventId, LocalDateTime created, Integer from, Integer size) {
        return null;
    }
}
