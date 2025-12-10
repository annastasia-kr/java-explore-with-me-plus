package ru.practicum.explorewithme.moderation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.model.CommentState;
import ru.practicum.explorewithme.comment.repository.CommentRepository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.moderation.dto.*;
import ru.practicum.explorewithme.moderation.mapper.ModerationLogMapper;
import ru.practicum.explorewithme.moderation.model.ModerationLog;
import ru.practicum.explorewithme.moderation.model.ModerationQueue;
import ru.practicum.explorewithme.moderation.repository.ModerationLogRepository;
import ru.practicum.explorewithme.moderation.repository.ModerationQueueRepository;
import ru.practicum.explorewithme.moderation.repository.ModerationRuleRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock
    private ModerationLogRepository moderationLogRepository;

    @Mock
    private ModerationQueueRepository moderationQueueRepository;

    @Mock
    private ModerationRuleRepository moderationRuleRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModerationLogMapper moderationLogMapper;

    @Mock
    private ModerationQueueMapper moderationQueueMapper;

    @Mock
    private ContentAnalyzerService contentAnalyzerService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ModerationService moderationService;

    private User moderator;
    private User author;
    private Comment comment;
    private Event event;
    private ModerationLog moderationLog;
    private ModerationQueue moderationQueue;

    @BeforeEach
    void setUp() {
        moderator = User.builder()
                .id(1L)
                .name("Moderator")
                .email("moderator@email.com")
                .build();

        author = User.builder()
                .id(2L)
                .name("Author")
                .email("author@email.com")
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Test comment with spam keyword")
                .state(CommentState.SUBMITTED)
                .created(LocalDateTime.now())
                .author(author)
                .build();

        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .annotation("Test annotation")
                .description("Test description")
                .state(ru.practicum.explorewithme.event.model.EventState.PUBLISHED)
                .initiator(author)
                .build();

        moderationLog = ModerationLog.builder()
                .id(1L)
                .entityType("COMMENT")
                .entityId(1L)
                .moderator(moderator)
                .action("APPROVE")
                .reason("Test reason")
                .previousState("SUBMITTED")
                .newState("APPROVED")
                .createdAt(LocalDateTime.now())
                .priorityLevel(2)
                .autoModerated(false)
                .build();

        moderationQueue = ModerationQueue.builder()
                .id(1L)
                .entityType("COMMENT")
                .entityId(1L)
                .comment(comment)
                .priorityScore(3)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .flagsCount(1)
                .autoFlagReasons("spam")
                .build();
    }

    @Test
    void logModeration_whenValidData_thenSuccess() {
        when(moderationLogRepository.save(any(ModerationLog.class))).thenReturn(moderationLog);
        when(moderationLogMapper.toDto(any(ModerationLog.class))).thenReturn(
                ModerationLogDto.builder()
                        .id(1L)
                        .entityType("COMMENT")
                        .entityId(1L)
                        .moderatorId(1L)
                        .moderatorName("Moderator")
                        .action("APPROVE")
                        .reason("Test reason")
                        .priorityLevel(2)
                        .severity("MEDIUM")
                        .build()
        );

        ModerationLogDto result = moderationService.logModeration(
                moderator, "COMMENT", 1L, "APPROVE", "Test reason",
                "SUBMITTED", "APPROVED", false, 2);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("COMMENT", result.getEntityType());
        assertEquals("APPROVE", result.getAction());
        assertEquals("MEDIUM", result.getSeverity());

        verify(moderationLogRepository, times(1)).save(any(ModerationLog.class));
    }

    @Test
    void getModerationLogs_withFilters_thenReturnPage() {
        Page<ModerationLog> page = new PageImpl<>(List.of(moderationLog));
        when(moderationLogRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);
        when(moderationLogMapper.toDto(any(ModerationLog.class))).thenReturn(
                ModerationLogDto.builder().id(1L).build());

        Page<ModerationLogDto> result = moderationService.getModerationLogs(
                "COMMENT", 1L, 1L, "APPROVE",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                false, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void autoModerateOldComments_whenOldCommentsExist_thenAutoApprove() {
        Comment oldComment = Comment.builder()
                .id(2L)
                .text("Old comment")
                .state(CommentState.SUBMITTED)
                .created(LocalDateTime.now().minusDays(8))
                .build();

        when(commentRepository.findAll()).thenReturn(List.of(oldComment));

        moderationService.autoModerateOldComments();

        assertEquals(CommentState.APPROVED, oldComment.getState());
        verify(commentRepository, times(1)).save(oldComment);
    }

    @Test
    void checkAndFlagSuspiciousContent_whenSpamDetected_thenFlag() {
        when(commentRepository.findAll()).thenReturn(List.of(comment));
        when(eventRepository.findAll()).thenReturn(List.of(event));

        ContentAnalysisResult analysisResult = ContentAnalysisResult.builder()
                .entityId(1L)
                .entityType("COMMENT")
                .content(comment.getText())
                .spamScore(0.8)
                .toxicityScore(0.1)
                .requiresModeration(true)
                .flaggedKeywords(List.of("spam"))
                .analysisSummary("Spam detected")
                .build();

        when(contentAnalyzerService.analyzeContent(anyString(), anyString(), anyLong()))
                .thenReturn(analysisResult);

        moderationService.checkAndFlagSuspiciousContent();

        verify(moderationQueueRepository, times(1)).save(any(ModerationQueue.class));
        verify(moderationLogRepository, times(1)).save(any(ModerationLog.class));
    }

    @Test
    void assignNextItem_whenPendingItemsExist_thenAssign() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(moderator));
        when(moderationQueueRepository.countInProgressByModerator(1L)).thenReturn(0L);

        Page<ModerationQueue> page = new PageImpl<>(List.of(moderationQueue));
        when(moderationQueueRepository.findPendingByPriority(any(PageRequest.class)))
                .thenReturn(page);

        when(moderationQueueRepository.save(any(ModerationQueue.class))).thenReturn(moderationQueue);
        when(moderationQueueMapper.toDto(any(ModerationQueue.class))).thenReturn(
                ModerationQueueDto.builder().id(1L).status("IN_PROGRESS").build());

        ModerationQueueDto result = moderationService.assignNextItem(1L);

        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(moderationQueueRepository, times(1)).save(any(ModerationQueue.class));
    }

    @Test
    void getModerationStats_whenValidPeriod_thenReturnStats() {
        when(moderationLogRepository.countByPeriod(any(), any())).thenReturn(10L);
        when(moderationQueueRepository.countPending()).thenReturn(5L);
        when(moderationLogRepository.countByEntityType(any(), any()))
                .thenReturn(List.of(new Object[]{"COMMENT", 7L}, new Object[]{"EVENT", 3L}));
        when(moderationLogRepository.getTopModerators(any(), any()))
                .thenReturn(List.of(new Object[]{"Moderator1", 6L}, new Object[]{"Moderator2", 4L}));
        when(moderationQueueRepository.getAverageResolutionTime()).thenReturn(30.5);

        ModerationStats stats = moderationService.getModerationStats(
                LocalDateTime.now().minusDays(30), LocalDateTime.now());

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalModerations());
        assertEquals(5L, stats.getPendingItems());
        assertEquals(7L, stats.getCommentModerations());
        assertEquals(3L, stats.getEventModerations());
        assertEquals(30.5, stats.getAverageResolutionTime());
    }
}