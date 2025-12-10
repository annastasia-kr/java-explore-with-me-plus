package ru.practicum.explorewithme.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.*;
import ru.practicum.explorewithme.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.model.CommentState;
import ru.practicum.explorewithme.comment.repository.CommentRepository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.ForbiddenException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentDetailDto createComment(Long eventId, CreateCommentDto createCommentDto) {
        log.info("Creating comment for event {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }

        User author = userRepository.findById(createCommentDto.getAuthorId())
                .orElseThrow(() -> new NotFoundException("Author not found"));

        Comment comment = Comment.builder()
                .text(createCommentDto.getText())
                .event(event)
                .author(author)
                .state(CommentState.SUBMITTED)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created with id {}", savedComment.getId());

        return commentMapper.toDetailDto(savedComment);
    }

    @Transactional
    public CommentDetailDto updateComment(Long eventId, Long commentId, UpdateCommentDto updateCommentDto) {
        log.info("Updating comment {} for event {}", commentId, eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found");
        }

        if (!userRepository.existsById(updateCommentDto.getAuthorId())) {
            throw new NotFoundException("Author not found");
        }

        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(updateCommentDto.getAuthorId())) {
            throw new ForbiddenException("Author is not real author");
        }

        comment.setText(updateCommentDto.getText());

        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toDetailDto(updatedComment);
    }

    @Transactional
    public void deleteComment(Long eventId, Long commentId) {
        log.info("Deleting comment {} for event {}", commentId, eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found");
        }

        if (!commentRepository.existsByIdAndEventId(commentId, eventId)) {
            throw new NotFoundException("Comment not found");
        }

        commentRepository.deleteById(commentId);
        log.info("Comment {} deleted", commentId);
    }

    public CommentDetailDto getComment(Long eventId, Long commentId) {
        log.info("Getting comment {} for event {}", commentId, eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found");
        }

        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        return commentMapper.toDetailDto(comment);
    }

    public List<CommentDetailDto> getComments(Long eventId, String stateStr, Long authorId, Integer from, Integer size) {
        log.info("Getting comments for event {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found");
        }

        CommentState state = null;
        if (stateStr != null) {
            try {
                state = CommentState.valueOf(stateStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllWithFilters(eventId, authorId, state, pageable);

        return comments.stream()
                .map(commentMapper::toDetailDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDetailDto moderateComment(Long eventId, Long commentId, UpdateAdminCommentDto updateAdminCommentDto) {
        log.info("Moderating comment {} for event {}", commentId, eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event not found");
        }

        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        CommentState newState = CommentState.valueOf(updateAdminCommentDto.getState().toUpperCase());
        comment.setState(newState);

        Comment moderatedComment = commentRepository.save(comment);

        return commentMapper.toDetailDto(moderatedComment);
    }

    public List<CommentDto> getApprovedCommentsForEvent(Long eventId) {
        List<Comment> comments = commentRepository.findByEventIdAndState(eventId, CommentState.APPROVED);
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    public Map<Long, List<CommentDto>> getApprovedCommentsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }

        List<Comment> comments = commentRepository.findByEventIdInAndState(eventIds, CommentState.APPROVED);

        return comments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getEvent().getId(),
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())
                ));
    }

    public List<CommentDetailDto> getAllCommentsForAdmin(Long eventId, String state, Long authorId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        CommentState commentState = null;

        if (state != null) {
            try {
                commentState = CommentState.valueOf(state.toUpperCase());
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }

        List<Comment> comments = commentRepository.findAllWithFilters(eventId, authorId, commentState, pageable);

        return comments.stream()
                .map(commentMapper::toDetailDto)
                .collect(Collectors.toList());
    }
}