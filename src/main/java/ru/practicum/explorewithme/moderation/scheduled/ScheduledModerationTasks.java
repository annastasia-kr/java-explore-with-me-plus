package ru.practicum.explorewithme.moderation.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.moderation.service.ModerationService;
import ru.practicum.explorewithme.moderation.service.NotificationService;
import ru.practicum.explorewithme.moderation.service.ReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledModerationTasks {

    private final ModerationService moderationService;
    private final NotificationService notificationService;
    private final ReportService reportService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Каждый час
    @Scheduled(cron = "0 0 * * * *")
    public void performAutoModeration() {
        log.info("Starting scheduled auto-moderation at {}", LocalDateTime.now().format(FORMATTER));

        try {
            moderationService.autoModerateAllPending().join();
            log.info("Scheduled auto-moderation completed");
        } catch (Exception e) {
            log.error("Scheduled auto-moderation failed", e);
        }
    }

    // Каждые 6 часов
    @Scheduled(cron = "0 0 */6 * * *")
    public void escalateStuckItems() {
        log.info("Starting scheduled escalation at {}", LocalDateTime.now().format(FORMATTER));

        try {
            moderationService.escalateStuckItems();
            log.info("Scheduled escalation completed");
        } catch (Exception e) {
            log.error("Scheduled escalation failed", e);
        }
    }

    // Ежедневно в 8:00
    @Scheduled(cron = "0 0 8 * * *")
    public void generateDailyReport() {
        log.info("Generating daily moderation report at {}", LocalDateTime.now().format(FORMATTER));

        try {
            String report = reportService.generateDailyReport();
            notificationService.sendDailyReport("admin@example.com", report);
            log.info("Daily report generated and sent");
        } catch (Exception e) {
            log.error("Daily report generation failed", e);
        }
    }

    // Еженедельно в понедельник в 9:00
    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklyReport() {
        log.info("Generating weekly moderation report at {}", LocalDateTime.now().format(FORMATTER));

        try {
            String report = reportService.generateWeeklyReport();
            notificationService.sendDailyReport("admin@example.com", report);
            log.info("Weekly report generated and sent");
        } catch (Exception e) {
            log.error("Weekly report generation failed", e);
        }
    }

    // Каждые 30 минут проверяем очередь
    @Scheduled(cron = "0 */30 * * * *")
    public void checkQueueStatus() {
        log.debug("Checking moderation queue status at {}", LocalDateTime.now().format(FORMATTER));

        try {
            Long pendingCount = moderationService.getPendingQueueCount();
            if (pendingCount > 100) {
                log.warn("Moderation queue has {} pending items", pendingCount);
                notificationService.sendSlackNotification(
                        String.format("⚠️ Moderation queue alert: %d pending items", pendingCount));
            }
        } catch (Exception e) {
            log.error("Queue status check failed", e);
        }
    }
}