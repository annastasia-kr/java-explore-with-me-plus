package ru.practicum.explorewithme.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.moderation.model.Comment;
import ru.practicum.explorewithme.moderation.model.CommentState;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByState(CommentState state);

    List<Comment> findByAuthorId(Long authorId);
}