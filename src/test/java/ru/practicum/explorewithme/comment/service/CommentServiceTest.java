package ru.practicum.explorewithme.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private User otherUser;
    private Event publishedEvent;
    private Event unpublishedEvent;
    private Comment comment;
    private CreateCommentDto createCommentDto;
    private UpdateCommentDto updateCommentDto;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .name("Author")
                .email("author@email.com")
                .build();

        otherUser = User.builder()
                .id(2L)
                .name("Other User")
                .email("other@email.com")
                .build();

        publishedEvent = Event.builder()
                .id(1L)
                .title("Published Event")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .state(EventState.PUBLISHED)
                .build();

        unpublishedEvent = Event.builder()
                .id(2L)
                .title("Unpublished Event")
                .annotation("Annotation")
                .description("Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .state(EventState.PENDING)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Original comment text")
                .event(publishedEvent)
                .author(author)
                .state(CommentState.SUBMITTED)
                .created(LocalDateTime.now())
                .build();

        createCommentDto = new CreateCommentDto("Test comment", 1L);
        updateCommentDto = new UpdateCommentDto("Updated comment text", 1L);
    }

    @Test
    void createComment_whenEventExistsAndPublishedAndAuthorExists_thenSuccess() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(publishedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDetailDto(any(Comment.class))).thenReturn(
                new CommentDetailDto(1L, "Test comment", 1L, "SUBMITTED",
                        LocalDateTime.now(), null));

        CommentDetailDto result = commentService.createComment(1L, createCommentDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test comment", result.getText());
        assertEquals("SUBMITTED", result.getState());

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_whenEventNotFound_thenThrowNotFoundException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.createComment(1L, createCommentDto));
    }

    @Test
    void createComment_whenEventNotPublished_thenThrowConflictException() {
        when(eventRepository.findById(2L)).thenReturn(Optional.of(unpublishedEvent));

        CreateCommentDto dto = new CreateCommentDto("Test comment", 1L);

        assertThrows(ConflictException.class,
                () -> commentService.createComment(2L, dto));
    }

    @Test
    void createComment_whenAuthorNotFound_thenThrowNotFoundException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(publishedEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.createComment(1L, createCommentDto));
    }

    @Test
    void updateComment_whenValidData_thenSuccess() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDetailDto(any(Comment.class))).thenReturn(
                new CommentDetailDto(1L, "Updated comment text", 1L, "SUBMITTED",
                        LocalDateTime.now(), LocalDateTime.now()));

        CommentDetailDto result = commentService.updateComment(1L, 1L, updateCommentDto);

        assertNotNull(result);
        assertEquals("Updated comment text", result.getText());

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void updateComment_whenEventNotFound_thenThrowNotFoundException() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.updateComment(1L, 1L, updateCommentDto));
    }

    @Test
    void updateComment_whenAuthorNotFound_thenThrowNotFoundException() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.updateComment(1L, 1L, updateCommentDto));
    }

    @Test
    void updateComment_whenCommentNotFound_thenThrowNotFoundException() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.updateComment(1L, 1L, updateCommentDto));
    }

    @Test
    void updateComment_whenAuthorIsNotRealAuthor_thenThrowForbiddenException() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(commentRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.of(comment));

        UpdateCommentDto wrongAuthorDto = new UpdateCommentDto("Updated text", 2L);

        assertThrows(ForbiddenException.class,
                () -> commentService.updateComment(1L, 1L, wrongAuthorDto));
    }

    @Test
    void deleteComment_whenValidData_thenSuccess() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.existsByIdAndEventId(1L, 1L)).thenReturn(true);

        commentService.deleteComment(1L, 1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteComment_whenEventNotFound_thenThrowNotFoundException() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.deleteComment(1L, 1L));
    }

    @Test
    void deleteComment_whenCommentNotFound_thenThrowNotFoundException() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.existsByIdAndEventId(1L, 1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.deleteComment(1L, 1L));
    }

    @Test
    void getComments_whenValidData_thenReturnList() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAllWithFilters(eq(1L), eq(null), eq(null), any(PageRequest.class)))
                .thenReturn(List.of(comment));
        when(commentMapper.toDetailDto(any(Comment.class))).thenReturn(
                new CommentDetailDto(1L, "Comment text", 1L, "SUBMITTED",
                        LocalDateTime.now(), null));

        List<CommentDetailDto> result = commentService.getComments(1L, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Comment text", result.get(0).getText());
    }

    @Test
    void moderateComment_whenValidData_thenSuccess() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByIdAndEventId(1L, 1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDetailDto(any(Comment.class))).thenReturn(
                new CommentDetailDto(1L, "Comment text", 1L, "APPROVED",
                        LocalDateTime.now(), null));

        UpdateAdminCommentDto adminDto = new UpdateAdminCommentDto("APPROVED");
        CommentDetailDto result = commentService.moderateComment(1L, 1L, adminDto);

        assertNotNull(result);
        assertEquals("APPROVED", result.getState());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}