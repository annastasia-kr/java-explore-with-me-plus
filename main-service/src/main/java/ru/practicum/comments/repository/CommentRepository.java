package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comments.enums.StateComment;
import ru.practicum.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByIdAndEventId(Long commentId, Long eventId);

    List<Comment> findByEventIdAndState(Long eventId, StateComment state);

    List<Comment> findByEventIdInAndState(List<Long> eventIds, StateComment state);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:eventId IS NULL OR c.event.id = :eventId) " +
            "AND (:authorId IS NULL OR c.author.id = :authorId) " +
            "AND (:state IS NULL OR c.state = :state)")
    List<Comment> findAllWithFilters(@Param("eventId") Long eventId,
                                     @Param("authorId") Long authorId,
                                     @Param("state") StateComment state,
                                     Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId")
    List<Comment> findByEventId(@Param("eventId") Long eventId, Pageable pageable);
}
