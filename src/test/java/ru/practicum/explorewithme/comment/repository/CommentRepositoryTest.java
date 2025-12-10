package ru.practicum.explorewithme.comment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.model.CommentState;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CommentRepository commentRepository;

    private User author;
    private Event event1;
    private Event event2;
    private Comment comment1;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .name("Author")
                .email("author@email.com")
                .build();
        em.persist(author);

        User otherUser = User.builder()
                .name("Other User")
                .email("other@email.com")
                .build();
        em.persist(otherUser);

        event1 = Event.builder()
                .title("Event 1")
                .annotation("Annotation 1")
                .description("Description 1")
                .eventDate(LocalDateTime.now().plusDays(1))
                .state(EventState.PUBLISHED)
                .initiator(author)
                .participantLimit(0)
                .paid(false)
                .requestModeration(true)
                .build();
        em.persist(event1);

        event2 = Event.builder()
                .title("Event 2")
                .annotation("Annotation 2")
                .description("Description 2")
                .eventDate(LocalDateTime.now().plusDays(2))
                .state(EventState.PUBLISHED)
                .initiator(otherUser)
                .participantLimit(0)
                .paid(false)
                .requestModeration(true)
                .build();
        em.persist(event2);

        comment1 = Comment.builder()
                .text("Comment 1 - SUBMITTED")
                .event(event1)
                .author(author)
                .state(CommentState.SUBMITTED)
                .created(LocalDateTime.now())
                .build();
        em.persist(comment1);

        Comment comment2 = Comment.builder()
                .text("Comment 2 - APPROVED")
                .event(event1)
                .author(author)
                .state(CommentState.APPROVED)
                .created(LocalDateTime.now())
                .build();
        em.persist(comment2);

        Comment comment3 = Comment.builder()
                .text("Comment 3 - APPROVED for event2")
                .event(event2)
                .author(otherUser)
                .state(CommentState.APPROVED)
                .created(LocalDateTime.now())
                .build();
        em.persist(comment3);

        em.flush();
    }

    @Test
    void findByIdAndEventId_whenExists_thenReturnComment() {
        Comment found = commentRepository.findByIdAndEventId(comment1.getId(), event1.getId())
                .orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(comment1.getId());
        assertThat(found.getEvent().getId()).isEqualTo(event1.getId());
    }

    @Test
    void findByIdAndEventId_whenNotExists_thenReturnEmpty() {
        assertThat(commentRepository.findByIdAndEventId(999L, event1.getId()))
                .isEmpty();
    }

    @Test
    void findByEventIdAndState_whenApproved_thenReturnApprovedComments() {
        List<Comment> approvedComments = commentRepository.findByEventIdAndState(
                event1.getId(), CommentState.APPROVED);

        assertThat(approvedComments).hasSize(1);
        assertThat(approvedComments.get(0).getState()).isEqualTo(CommentState.APPROVED);
    }

    @Test
    void findByEventIdInAndState_whenApproved_thenReturnApprovedComments() {
        List<Comment> approvedComments = commentRepository.findByEventIdInAndState(
                List.of(event1.getId(), event2.getId()), CommentState.APPROVED);

        assertThat(approvedComments).hasSize(2);
        assertThat(approvedComments).extracting(Comment::getState)
                .containsOnly(CommentState.APPROVED);
    }

    @Test
    void findAllWithFilters_whenFilterByEventId_thenReturnFiltered() {
        List<Comment> comments = commentRepository.findAllWithFilters(
                event1.getId(), null, null, PageRequest.of(0, 10));

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getEvent)
                .extracting(Event::getId)
                .containsOnly(event1.getId());
    }

    @Test
    void findAllWithFilters_whenFilterByAuthorId_thenReturnFiltered() {
        List<Comment> comments = commentRepository.findAllWithFilters(
                null, author.getId(), null, PageRequest.of(0, 10));

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getAuthor)
                .extracting(User::getId)
                .containsOnly(author.getId());
    }

    @Test
    void findAllWithFilters_whenFilterByState_thenReturnFiltered() {
        List<Comment> comments = commentRepository.findAllWithFilters(
                null, null, CommentState.APPROVED, PageRequest.of(0, 10));

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getState)
                .containsOnly(CommentState.APPROVED);
    }
}