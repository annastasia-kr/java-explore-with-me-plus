package ru.practicum.comments.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.*;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.enums.StateEvent;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exception.AccessDeniedForUserException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final EventRepository eventRepository;

    @Override
    public Collection<CommentDetailDto> getCommentsByAdmin(Integer from, Integer size, List<StateComment> states,
                                                           List<Long> authorsIds, Long eventId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> criteriaQuery = cb.createQuery(Comment.class);
        Root<Comment> root = criteriaQuery.from(Comment.class);

        List<Predicate> predicates = new ArrayList<>();
        applyAuthorFilter(predicates, root, authorsIds);
        applyStateFilter(predicates, root, states);
        applyEventFilter(predicates, cb, root, eventId);

        if (!predicates.isEmpty()) {
            criteriaQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<Comment> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        List<Comment> comments = typedQuery.getResultList();

        if (comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(commentMapper::toCommentDetailDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDetailDto moderateCommentById(Long commentId, Long eventId, UpdateCommentDtoAdmin updateCommentDtoAdmin) {
        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found"));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new DataConflictException("The comment is not associated with the given event");
        }

        if(updateCommentDtoAdmin.getState() != null) {
            comment.setState(updateCommentDtoAdmin.getState());
        }
        return commentMapper.toCommentDetailDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDetailDto createComment(Long eventId, CreateCommentDto createCommentDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        if (event.getState() != StateEvent.PUBLISHED) {
            throw new DataConflictException("Event is not published");
        }
        User user = userRepository.findById(createCommentDto.getAuthorId()).orElseThrow(
                () -> new NotFoundException("Author not found"));
        Comment createdComment = commentMapper.toComment(createCommentDto, event, user);

        return commentMapper.toCommentDetailDto(commentRepository.save(createdComment));
    }

    @Override
    @Transactional
    public CommentDetailDto updateCommentByUser(Long eventId, Long commentId, UpdateCommentDto updateCommentDto) {
        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        userRepository.findById(updateCommentDto.getAuthorId()).orElseThrow(
                () -> new NotFoundException("Author not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found"));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new DataConflictException("The comment is not associated with the given event");
        }
        if (!comment.getAuthor().getId().equals(updateCommentDto.getAuthorId())) {
            throw new AccessDeniedForUserException("User is not the author of this comment");
        }
        if(updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }
        comment.setLastEdited(LocalDateTime.now());
        return commentMapper.toCommentDetailDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment() {
    }

    @Override
    public CommentDetailDto getCommentById(Long commentId, Long eventId) {
        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment not found"));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new DataConflictException("The comment is not associated with the given event");
        }
        return commentMapper.toCommentDetailDto(comment);
    }

    private void applyStateFilter(List<Predicate> predicates, Root<Comment> root, List<StateComment> states) {
        if (states != null && !states.isEmpty()) {
            predicates.add(root.get("state").in(states));
        }
    }

    private void applyAuthorFilter(List<Predicate> predicates, Root<Comment> root, List<Long> authors) {
        if (authors != null && !authors.isEmpty()) {
            predicates.add(root.get("author").get("id").in(authors));
        }
    }

    private void applyEventFilter(List<Predicate> predicates, CriteriaBuilder cb, Root<Comment> root, Long id) {
        if (id != null) {
            predicates.add(cb.equal(root.get("eventId"), id));
        }
    }
}
