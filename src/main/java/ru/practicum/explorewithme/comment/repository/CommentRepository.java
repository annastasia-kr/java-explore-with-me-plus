package ru.practicum.explorewithme.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.model.CommentState;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByIdAndEventId(Long commentId, Long eventId);

    Optional<Comment> findByIdAndEventId(Long commentId, Long eventId);

    List<Comment> findByEventIdAndState(Long eventId, CommentState state);

    List<Comment> findByEventIdInAndState(List<Long> eventIds, CommentState state);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:eventId IS NULL OR c.event.id = :eventId) " +
            "AND (:authorId IS NULL OR c.author.id = :authorId) " +
            "AND (:state IS NULL OR c.state = :state)")
    List<Comment> findAllWithFilters(@Param("eventId") Long eventId,
                                     @Param("authorId") Long authorId,
                                     @Param("state") CommentState state,
                                     Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId")
    List<Comment> findByEventId(@Param("eventId") Long eventId, Pageable pageable);
}