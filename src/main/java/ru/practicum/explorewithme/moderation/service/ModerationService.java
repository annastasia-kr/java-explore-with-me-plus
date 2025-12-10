package ru.practicum.explorewithme.moderation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.model.CommentState;
import ru.practicum.explorewithme.comment.repository.CommentRepository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.moderation.dto.*;
import ru.practicum.explorewithme.moderation.mapper.ModerationLogMapper;
import ru.practicum.explorewithme.moderation.mapper.ModerationRuleMapper;
import ru.practicum.explorewithme.moderation.model.ModerationLog;
import ru.practicum.explorewithme.moderation.model.ModerationQueue;
import ru.practicum.explorewithme.moderation.model.ModerationRule;
import ru.practicum.explorewithme.moderation.repository.ModerationLogRepository;
import ru.practicum.explorewithme.moderation.repository.ModerationQueueRepository;
import ru.practicum.explorewithme.moderation.repository.ModerationRuleRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModerationService {

    private final ModerationLogRepository moderationLogRepository;
    private final ModerationQueueRepository moderationQueueRepository;
    private final ModerationRuleRepository moderationRuleRepository;
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final ModerationLogMapper moderationLogMapper;
    private final ModerationQueueMapper moderationQueueMapper;
    private final ModerationRuleMapper moderationRuleMapper;

    private final ContentAnalyzerService contentAnalyzerService;
    private final NotificationService notificationService;

    private static final List<String> BANNED_KEYWORDS = Arrays.asList(
            "spam", "scam", "fraud", "fake", "phishing",
            "hate", "racism", "discrimination", "violence",
            "porn", "adult", "explicit", "nsfw"
    );

    // Основные методы логгирования
    @Transactional
    public ModerationLogDto logModeration(User moderator, String entityType, Long entityId,
                                          String action, String reason, String previousState,
                                          String newState, Boolean autoModerated, Integer priorityLevel) {
        log.info("Logging moderation: {} {} {} by {}", action, entityType, entityId,
                moderator != null ? moderator.getName() : "AUTO");

        ModerationLog logEntry = ModerationLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .moderator(moderator)
                .action(action)
                .reason(reason)
                .previousState(previousState)
                .newState(newState)
                .autoModerated(autoModerated != null ? autoModerated : false)
                .priorityLevel(priorityLevel != null ? priorityLevel : 1)
                .createdAt(LocalDateTime.now())
                .build();

        ModerationLog saved = moderationLogRepository.save(logEntry);

        // Отправляем уведомление, если требуется
        if (priorityLevel != null && priorityLevel >= 4) {
            notificationService.sendHighPriorityNotification(saved);
        }

        return moderationLogMapper.toDto(saved);
    }

    // Получение логов с фильтрацией
    public Page<ModerationLogDto> getModerationLogs(String entityType, Long entityId, Long moderatorId,
                                                    String action, LocalDateTime start, LocalDateTime end,
                                                    Boolean autoModerated, Integer page, Integer size) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<ModerationLog> logs = moderationLogRepository.findWithFilters(
                entityType, entityId, moderatorId, action, start, end, autoModerated, pageable);

        return logs.map(moderationLogMapper::toDto);
    }

    // Автоматическая модерация
    @Async
    @Transactional
    public CompletableFuture<Void> autoModerateAllPending() {
        log.info("Starting auto-moderation of all pending content");

        try {
            // 1. Автомодерация старых комментариев
            autoModerateOldComments();

            // 2. Проверка подозрительного контента
            checkAndFlagSuspiciousContent();

            // 3. Применение правил модерации
            applyModerationRules();

            // 4. Эскалация застрявших задач
            escalateStuckItems();

            log.info("Auto-moderation completed successfully");

        } catch (Exception e) {
            log.error("Error during auto-moderation", e);
            throw new RuntimeException("Auto-moderation failed", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void autoModerateOldComments() {
        LocalDateTime threshold = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        List<Comment> oldComments = commentRepository.findAll().stream()
                .filter(comment -> comment.getState() == CommentState.SUBMITTED)
                .filter(comment -> comment.getCreated().isBefore(threshold))
                .collect(Collectors.toList());

        for (Comment comment : oldComments) {
            String previousState = comment.getState().toString();
            comment.setState(CommentState.APPROVED);
            commentRepository.save(comment);

            logModeration(null, "COMMENT", comment.getId(), "AUTO_APPROVE",
                    "Automatically approved after 7 days", previousState,
                    CommentState.APPROVED.toString(), true, 1);
        }

        log.info("Auto-moderated {} old comments", oldComments.size());
    }

    @Transactional
    public void checkAndFlagSuspiciousContent() {
        log.info("Checking for suspicious content");

        List<Comment> allComments = commentRepository.findAll();
        List<Event> allEvents = eventRepository.findAll();

        // Проверка комментариев
        for (Comment comment : allComments) {
            ContentAnalysisResult analysis = contentAnalyzerService.analyzeContent(
                    comment.getText(), "COMMENT", comment.getId());

            if (analysis.isRequiresModeration()) {
                flagContentForReview(comment, analysis);
            }
        }

        // Проверка событий
        for (Event event : allEvents) {
            String content = event.getTitle() + " " + event.getAnnotation() + " " + event.getDescription();
            ContentAnalysisResult analysis = contentAnalyzerService.analyzeContent(
                    content, "EVENT", event.getId());

            if (analysis.isRequiresModeration()) {
                flagContentForReview(event, analysis);
            }
        }
    }

    private void flagContentForReview(Comment comment, ContentAnalysisResult analysis) {
        ModerationQueue queueItem = ModerationQueue.builder()
                .entityType("COMMENT")
                .entityId(comment.getId())
                .comment(comment)
                .priorityScore(calculatePriorityScore(analysis))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .flagsCount(1)
                .autoFlagReasons(String.join(", ", analysis.getFlaggedKeywords()))
                .build();

        moderationQueueRepository.save(queueItem);

        logModeration(null, "COMMENT", comment.getId(), "AUTO_FLAG",
                "Flagged by content analyzer: " + analysis.getAnalysisSummary(),
                comment.getState().toString(), "FLAGGED", true,
                queueItem.getPriorityScore());
    }

    private void flagContentForReview(Event event, ContentAnalysisResult analysis) {
        ModerationQueue queueItem = ModerationQueue.builder()
                .entityType("EVENT")
                .entityId(event.getId())
                .event(event)
                .priorityScore(calculatePriorityScore(analysis))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .flagsCount(1)
                .autoFlagReasons(String.join(", ", analysis.getFlaggedKeywords()))
                .build();

        moderationQueueRepository.save(queueItem);

        logModeration(null, "EVENT", event.getId(), "AUTO_FLAG",
                "Flagged by content analyzer: " + analysis.getAnalysisSummary(),
                event.getState().toString(), "FLAGGED", true,
                queueItem.getPriorityScore());
    }

    private Integer calculatePriorityScore(ContentAnalysisResult analysis) {
        int score = 1;

        if (analysis.getToxicityScore() > 0.7) score += 3;
        if (analysis.getSpamScore() > 0.7) score += 2;
        if (analysis.getSentimentScore() < -0.5) score += 1;
        if (!analysis.getFlaggedKeywords().isEmpty()) score += analysis.getFlaggedKeywords().size();

        return Math.min(score, 5); // Максимум 5
    }

    // Работа с очередью модерации
    @Transactional
    public ModerationQueueDto assignNextItem(Long moderatorId) {
        log.info("Assigning next item to moderator {}", moderatorId);

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NotFoundException("Moderator not found"));

        // Проверяем, сколько уже задач у модератора
        Long inProgressCount = moderationQueueRepository.countInProgressByModerator(moderatorId);
        if (inProgressCount >= 5) {
            throw new IllegalStateException("Moderator has too many assigned tasks");
        }

        // Находим следующую задачу
        PageRequest pageable = PageRequest.of(0, 1);
        Page<ModerationQueue> pendingItems = moderationQueueRepository.findPendingByPriority(pageable);

        if (pendingItems.isEmpty()) {
            throw new NotFoundException("No pending items in queue");
        }

        ModerationQueue item = pendingItems.getContent().get(0);
        item.setAssignedTo(moderatorId);
        item.setAssignedAt(LocalDateTime.now());
        item.setStatus("IN_PROGRESS");

        ModerationQueue saved = moderationQueueRepository.save(item);

        // Логируем назначение
        logModeration(moderator, item.getEntityType(), item.getEntityId(),
                "ASSIGN", "Assigned to moderator", item.getStatus(), "IN_PROGRESS",
                false, item.getPriorityScore());

        return moderationQueueMapper.toDto(saved);
    }

    @Transactional
    public ModerationQueueDto resolveItem(Long queueId, Long moderatorId, String action, String resolution) {
        log.info("Resolving queue item {} by moderator {}", queueId, moderatorId);

        ModerationQueue item = moderationQueueRepository.findById(queueId)
                .orElseThrow(() -> new NotFoundException("Queue item not found"));

        if (!moderatorId.equals(item.getAssignedTo())) {
            throw new IllegalStateException("Item is not assigned to this moderator");
        }

        item.setStatus("RESOLVED");
        item.setResolvedAt(LocalDateTime.now());
        moderationQueueRepository.save(item);

        // Обновляем состояние сущности
        updateEntityState(item.getEntityType(), item.getEntityId(), action, resolution);

        // Логируем разрешение
        logModeration(userRepository.findById(moderatorId).orElse(null),
                item.getEntityType(), item.getEntityId(), action,
                resolution, "IN_PROGRESS", "RESOLVED", false,
                item.getPriorityScore());

        return moderationQueueMapper.toDto(item);
    }

    private void updateEntityState(String entityType, Long entityId, String action, String resolution) {
        switch (entityType) {
            case "COMMENT":
                Comment comment = commentRepository.findById(entityId)
                        .orElseThrow(() -> new NotFoundException("Comment not found"));

                switch (action) {
                    case "APPROVE":
                        comment.setState(CommentState.APPROVED);
                        break;
                    case "REJECT":
                        comment.setState(CommentState.REJECTED);
                        break;
                    case "DELETE":
                        commentRepository.delete(comment);
                        break;
                }
                commentRepository.save(comment);
                break;

            case "EVENT":
                // Аналогичная логика для событий
                break;
        }
    }

    // Управление правилами модерации
    @Transactional
    public ModerationRuleDto createRule(ModerationRuleDto ruleDto, Long createdById) {
        log.info("Creating moderation rule: {}", ruleDto.getRuleName());

        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (moderationRuleRepository.existsByRuleName(ruleDto.getRuleName())) {
            throw new IllegalStateException("Rule with this name already exists");
        }

        ModerationRule rule = ModerationRule.builder()
                .ruleName(ruleDto.getRuleName())
                .entityType(ruleDto.getEntityType())
                .conditionType(ruleDto.getConditionType())
                .conditionValue(ruleDto.getConditionValue())
                .action(ruleDto.getAction())
                .priorityLevel(ruleDto.getPriorityLevel())
                .isActive(ruleDto.getIsActive())
                .weight(ruleDto.getWeight())
                .description(ruleDto.getDescription())
                .createdBy(createdBy)
                .build();

        ModerationRule saved = moderationRuleRepository.save(rule);
        return moderationRuleMapper.toDto(saved);
    }

    @Transactional
    public void applyModerationRules() {
        log.info("Applying moderation rules");

        List<ModerationRule> activeRules = moderationRuleRepository.findByIsActiveTrue();

        for (ModerationRule rule : activeRules) {
            try {
                applyRule(rule);
            } catch (Exception e) {
                log.error("Error applying rule {}: {}", rule.getRuleName(), e.getMessage());
            }
        }
    }

    private void applyRule(ModerationRule rule) {
        switch (rule.getEntityType()) {
            case "COMMENT":
                applyRuleToComments(rule);
                break;
            case "EVENT":
                applyRuleToEvents(rule);
                break;
        }
    }

    private void applyRuleToComments(ModerationRule rule) {
        List<Comment> comments = commentRepository.findAll();

        for (Comment comment : comments) {
            if (matchesCondition(comment.getText(), rule.getConditionType(), rule.getConditionValue())) {
                executeAction(comment, rule);
            }
        }
    }

    private boolean matchesCondition(String content, String conditionType, String conditionValue) {
        content = content.toLowerCase();
        conditionValue = conditionValue.toLowerCase();

        switch (conditionType) {
            case "CONTAINS":
                return content.contains(conditionValue);
            case "CONTAINS_ANY":
                String[] keywords = conditionValue.split(",");
                return Arrays.stream(keywords).anyMatch(content::contains);
            case "REGEX":
                return content.matches(conditionValue);
            case "STARTS_WITH":
                return content.startsWith(conditionValue);
            case "ENDS_WITH":
                return content.endsWith(conditionValue);
            default:
                return false;
        }
    }

    private void executeAction(Comment comment, ModerationRule rule) {
        switch (rule.getAction()) {
            case "AUTO_REJECT":
                comment.setState(CommentState.REJECTED);
                commentRepository.save(comment);
                logModeration(null, "COMMENT", comment.getId(), "AUTO_REJECT",
                        "Automatically rejected by rule: " + rule.getRuleName(),
                        comment.getState().toString(), "REJECTED", true,
                        rule.getPriorityLevel());
                break;

            case "FLAG":
                ModerationQueue queueItem = ModerationQueue.builder()
                        .entityType("COMMENT")
                        .entityId(comment.getId())
                        .comment(comment)
                        .priorityScore(rule.getPriorityLevel())
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .flagsCount(1)
                        .autoFlagReasons("Rule: " + rule.getRuleName())
                        .build();
                moderationQueueRepository.save(queueItem);
                break;

            case "ESCALATE":
                // Эскалация к старшему модератору
                break;
        }
    }

    // Статистика
    public ModerationStats getModerationStats(LocalDateTime start, LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusDays(30);
        if (end == null) end = LocalDateTime.now();

        Long totalModerations = moderationLogRepository.countByPeriod(start, end);
        Long pendingItems = moderationQueueRepository.countPending();

        List<Object[]> byEntityType = moderationLogRepository.countByEntityType(start, end);
        Map<String, Long> entitiesByType = new HashMap<>();

        for (Object[] row : byEntityType) {
            entitiesByType.put((String) row[0], (Long) row[1]);
        }

        List<Object[]> topModerators = moderationLogRepository.getTopModerators(start, end);
        Map<String, Long> moderationsByUser = new HashMap<>();

        for (Object[] row : topModerators) {
            moderationsByUser.put((String) row[0], (Long) row[1]);
        }

        Double averageResolutionTime = moderationQueueRepository.getAverageResolutionTime();

        return ModerationStats.builder()
                .totalModerations(totalModerations)
                .pendingItems(pendingItems)
                .resolvedItems(totalModerations - pendingItems)
                .commentModerations(entitiesByType.getOrDefault("COMMENT", 0L))
                .eventModerations(entitiesByType.getOrDefault("EVENT", 0L))
                .userModerations(entitiesByType.getOrDefault("USER", 0L))
                .entitiesByType(entitiesByType)
                .moderationsByUser(moderationsByUser)
                .averageResolutionTime(averageResolutionTime)
                .periodStart(start)
                .periodEnd(end)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // Эскалация застрявших задач
    @Transactional
    public void escalateStuckItems() {
        log.info("Escalating stuck moderation items");

        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        List<ModerationQueue> stuckItems = moderationQueueRepository.findAll().stream()
                .filter(item -> "IN_PROGRESS".equals(item.getStatus()))
                .filter(item -> item.getAssignedAt() != null && item.getAssignedAt().isBefore(threshold))
                .collect(Collectors.toList());

        for (ModerationQueue item : stuckItems) {
            item.setStatus("ESCALATED");
            item.setAssignedTo(null);
            item.setAssignedAt(null);
            moderationQueueRepository.save(item);

            logModeration(null, item.getEntityType(), item.getEntityId(), "ESCALATE",
                    "Escalated due to inactivity for 24 hours", "IN_PROGRESS", "ESCALATED",
                    true, item.getPriorityScore() + 1);

            // Отправляем уведомление администратору
            notificationService.sendEscalationNotification(item);
        }

        log.info("Escalated {} stuck items", stuckItems.size());
    }

    // Поиск дубликатов
    public List<ContentAnalysisResult> findDuplicateContent(String entityType, String content, Double threshold) {
        log.info("Looking for duplicate content for type: {}", entityType);

        List<ContentAnalysisResult> duplicates = new ArrayList<>();
        LevenshteinDistance distance = new LevenshteinDistance();

        if ("COMMENT".equals(entityType)) {
            List<Comment> allComments = commentRepository.findAll();

            for (Comment comment : allComments) {
                Double similarity = calculateSimilarity(content, comment.getText(), distance);

                if (similarity >= threshold) {
                    ContentAnalysisResult result = ContentAnalysisResult.builder()
                            .entityId(comment.getId())
                            .entityType("COMMENT")
                            .content(comment.getText())
                            .sentimentScore(similarity)
                            .explanations(List.of("Potential duplicate content"))
                            .requiresModeration(true)
                            .recommendedAction("REVIEW_DUPLICATE")
                            .priorityLevel(3)
                            .build();

                    duplicates.add(result);
                }
            }
        }

        return duplicates;
    }

    private Double calculateSimilarity(String text1, String text2, LevenshteinDistance distance) {
        int maxLength = Math.max(text1.length(), text2.length());
        if (maxLength == 0) return 1.0;

        int editDistance = distance.apply(text1, text2);
        return 1.0 - ((double) editDistance / maxLength);
    }
}