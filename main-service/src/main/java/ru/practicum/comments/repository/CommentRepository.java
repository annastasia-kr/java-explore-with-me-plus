package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.model.Comment;

import java.util.Collection;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Collection<Comment> findAllByState(Pageable page, StateComment stateComment);

    Collection<Comment> findAllByEventId(Long eventId, Pageable page);
}
