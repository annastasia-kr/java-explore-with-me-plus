package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByState(StateComment stateComment, Pageable page);

    List<Comment> findAllByEvent_Id(Long eventId, Pageable page);
}
