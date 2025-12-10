package ru.practicum.explorewithme.moderation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.moderation.dto.*;
import ru.practicum.explorewithme.moderation.service.ModerationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    // Управление очередью модерации
    @GetMapping("/queue")
    public Page<ModerationQueueDto> getModerationQueue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @Positive Integer priorityMin,
            @RequestParam(required = false) @Positive Integer priorityMax,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAfter,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(defaultValue = "20") @Positive Integer size) {

        log.info("GET /admin/moderation/queue with filters");
        return moderationService.getModerationQueue(status, assignedTo, entityType,
                priorityMin, priorityMax, createdAfter, page, size);
    }

    @PostMapping("/queue/assign")
    public ModerationQueueDto assignNextItem(@RequestParam @Positive Long moderatorId) {
        log.info("POST /admin/moderation/queue/assign for moderator {}", moderatorId);
        return moderationService.assignNextItem(moderatorId);
    }

    @PostMapping("/queue/{queueId}/resolve")
    public ModerationQueueDto resolveItem(
            @PathVariable Long queueId,
            @RequestParam @Positive Long moderatorId,
            @RequestParam String action,
            @RequestParam(required = false) String resolution) {

        log.info("POST /admin/moderation/queue/{}/resolve", queueId);
        return moderationService.resolveItem(queueId, moderatorId, action, resolution);
    }

    // Логи модерации
    @GetMapping("/logs")
    public Page<ModerationLogDto> getModerationLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) Long moderatorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) Boolean autoModerated,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(defaultValue = "20") @Positive Integer size) {

        log.info("GET /admin/moderation/logs");
        return moderationService.getModerationLogs(entityType, entityId, moderatorId,
                action, start, end, autoModerated, page, size);
    }

    // Статистика
    @GetMapping("/stats")
    public ModerationStats getModerationStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {

        log.info("GET /admin/moderation/stats");
        return moderationService.getModerationStats(start, end);
    }

    // Автоматическая модерация
    @PostMapping("/auto-moderate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CompletableFuture<Void> triggerAutoModeration() {
        log.info("POST /admin/moderation/auto-moderate");
        return moderationService.autoModerateAllPending();
    }

    // Поиск дубликатов
    @GetMapping("/duplicates")
    public List<ContentAnalysisResult> findDuplicates(
            @RequestParam String entityType,
            @RequestParam String content,
            @RequestParam(defaultValue = "0.8") Double threshold) {

        log.info("GET /admin/moderation/duplicates for {}", entityType);
        return moderationService.findDuplicateContent(entityType, content, threshold);
    }

    // Эскалация
    @PostMapping("/escalate")
    @ResponseStatus(HttpStatus.OK)
    public void escalateStuckItems() {
        log.info("POST /admin/moderation/escalate");
        moderationService.escalateStuckItems();
    }
}