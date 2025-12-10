package ru.practicum.explorewithme.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.CommentDetailDto;
import ru.practicum.explorewithme.comment.dto.CreateCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateAdminCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User author;
    private User otherUser;
    private Event publishedEvent;
    private Event unpublishedEvent;

    CommentIntegrationTest(CommentService commentService) {
        this.commentService = commentService;
    }

    @BeforeEach
    void setUp() {
        author = User.builder()
                .name("Author")
                .email("author@email.com")
                .build();
        userRepository.save(author);

        otherUser = User.builder()
                .name("Other User")
                .email("other@email.com")
                .build();
        userRepository.save(otherUser);

        publishedEvent = Event.builder()
                .title("Published Event")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .state(EventState.PUBLISHED)
                .initiator(author)
                .participantLimit(0)
                .paid(false)
                .requestModeration(true)
                .build();
        eventRepository.save(publishedEvent);

        unpublishedEvent = Event.builder()
                .title("Unpublished Event")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .state(EventState.PENDING)
                .initiator(otherUser)
                .participantLimit(0)
                .paid(false)
                .requestModeration(true)
                .build();
        eventRepository.save(unpublishedEvent);
    }

    @Test
    void fullCommentLifecycleTest() {
        // 1. Create comment
        CreateCommentDto createDto = new CreateCommentDto("Test comment", author.getId());
        CommentDetailDto created = commentService.createComment(publishedEvent.getId(), createDto);

        assertNotNull(created.getId());
        assertEquals("Test comment", created.getText());
        assertEquals("SUBMITTED", created.getState());
        assertEquals(author.getId(), created.getAuthorId());

        // 2. Update comment
        UpdateCommentDto updateDto = new UpdateCommentDto("Updated comment", author.getId());
        CommentDetailDto updated = commentService.updateComment(
                publishedEvent.getId(), created.getId(), updateDto);

        assertEquals("Updated comment", updated.getText());

        // 3. Get comment
        CommentDetailDto retrieved = commentService.getComment(publishedEvent.getId(), created.getId());
        assertEquals(created.getId(), retrieved.getId());

        // 4. Moderate comment (approve)
        UpdateAdminCommentDto adminDto = new UpdateAdminCommentDto("APPROVED");
        CommentDetailDto moderated = commentService.moderateComment(
                publishedEvent.getId(), created.getId(), adminDto);

        assertEquals("APPROVED", moderated.getState());

        // 5. Get comments with filter
        List<CommentDetailDto> comments = commentService.getComments(
                publishedEvent.getId(), "APPROVED", null, 0, 10);

        assertThat(comments).hasSize(1);
        assertEquals(created.getId(), comments.get(0).getId());

        // 6. Get approved comments for event
        List<ru.practicum.explorewithme.comment.dto.CommentDto> approvedComments =
                commentService.getApprovedCommentsForEvent(publishedEvent.getId());

        assertThat(approvedComments).hasSize(1);

        // 7. Delete comment
        commentService.deleteComment(publishedEvent.getId(), created.getId());

        assertThrows(NotFoundException.class, () ->
                commentService.getComment(publishedEvent.getId(), created.getId()));
    }

    @Test
    void createCommentForUnpublishedEvent_shouldThrowConflictException() {
        CreateCommentDto createDto = new CreateCommentDto("Test comment", author.getId());

        assertThrows(ConflictException.class, () ->
                commentService.createComment(unpublishedEvent.getId(), createDto));
    }

    @Test
    void updateCommentWithWrongAuthor_shouldThrowForbiddenException() {
        // Create comment
        CreateCommentDto createDto = new CreateCommentDto("Test comment", author.getId());
        CommentDetailDto created = commentService.createComment(publishedEvent.getId(), createDto);

        // Try to update with wrong author
        UpdateCommentDto updateDto = new UpdateCommentDto("Updated comment", otherUser.getId());

        assertThrows(ForbiddenException.class, () ->
                commentService.updateComment(publishedEvent.getId(), created.getId(), updateDto));
    }

    @Test
    void moderateCommentWithInvalidState_shouldThrowIllegalArgumentException() {
        CreateCommentDto createDto = new CreateCommentDto("Test comment", author.getId());
        CommentDetailDto created = commentService.createComment(publishedEvent.getId(), createDto);

        UpdateAdminCommentDto invalidDto = new UpdateAdminCommentDto("INVALID_STATE");

        assertThrows(IllegalArgumentException.class, () ->
                commentService.moderateComment(publishedEvent.getId(), created.getId(), invalidDto));
    }
}
