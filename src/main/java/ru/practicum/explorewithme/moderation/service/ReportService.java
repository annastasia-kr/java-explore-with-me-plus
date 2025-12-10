package ru.practicum.explorewithme.moderation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.moderation.dto.ModerationStats;
import ru.practicum.explorewithme.moderation.repository.ModerationLogRepository;
import ru.practicum.explorewithme.moderation.repository.ModerationQueueRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ModerationService moderationService;
    private final ModerationLogRepository logRepository;
    private final ModerationQueueRepository queueRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String generateDailyReport() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        ModerationStats stats = moderationService.getModerationStats(start, end);
        Long pendingCount = queueRepository.countPending();
        List<Object[]> topModerators = logRepository.getTopModerators(start, end);

        StringBuilder report = new StringBuilder();
        report.append("=== Daily Moderation Report ===\n\n");
        report.append(String.format("Period: %s to %s\n",
                start.format(TIME_FORMATTER), end.format(TIME_FORMATTER)));
        report.append(String.format("Generated: %s\n\n", LocalDateTime.now().format(TIME_FORMATTER)));

        report.append("=== Statistics ===\n");
        report.append(String.format("Total Moderations: %d\n", stats.getTotalModerations()));
        report.append(String.format("Pending Items: %d\n", pendingCount));
        report.append(String.format("Comment Moderations: %d\n", stats.getCommentModerations()));
        report.append(String.format("Event Moderations: %d\n", stats.getEventModerations()));
        report.append(String.format("Average Resolution Time: %.2f minutes\n\n",
                stats.getAverageResolutionTime()));

        report.append("=== Top Moderators ===\n");
        int rank = 1;
        for (Object[] row : topModerators) {
            if (rank > 5) break;
            report.append(String.format("%d. %s: %d actions\n",
                    rank++, row[0], row[1]));
        }

        report.append("\n=== Recommendations ===\n");
        if (pendingCount > 50) {
            report.append("❌ Queue is large. Consider adding more moderators.\n");
        } else if (pendingCount > 20) {
            report.append("⚠️  Queue is moderate. Monitor closely.\n");
        } else {
            report.append("✅ Queue is healthy.\n");
        }

        if (stats.getAverageResolutionTime() > 120) {
            report.append("❌ Resolution time is too high. Review processes.\n");
        }

        return report.toString();
    }

    public String generateWeeklyReport() {
        LocalDateTime start = LocalDateTime.now().minusWeeks(1);
        LocalDateTime end = LocalDateTime.now();

        ModerationStats stats = moderationService.getModerationStats(start, end);
        Long pendingCount = queueRepository.countPending();

        StringBuilder report = new StringBuilder();
        report.append("=== Weekly Moderation Report ===\n\n");
        report.append(String.format("Period: %s to %s\n",
                start.format(DATE_FORMATTER), end.format(DATE_FORMATTER)));
        report.append(String.format("Generated: %s\n\n", LocalDateTime.now().format(TIME_FORMATTER)));

        report.append("=== Weekly Statistics ===\n");
        report.append(String.format("Total Moderations: %d\n", stats.getTotalModerations()));
        report.append(String.format("Average Daily: %.1f\n", stats.getTotalModerations() / 7.0));
        report.append(String.format("Comment Moderations: %d\n", stats.getCommentModerations()));
        report.append(String.format("Event Moderations: %d\n", stats.getEventModerations()));
        report.append(String.format("Escalation Rate: %.2f%%\n\n", stats.getEscalationRate()));

        report.append("=== Performance Metrics ===\n");
        report.append(String.format("Average Resolution Time: %.2f minutes\n",
                stats.getAverageResolutionTime()));
        report.append(String.format("Current Queue Size: %d items\n", pendingCount));

        report.append("\n=== Trends ===\n");
        // Здесь можно добавить анализ трендов за неделю

        report.append("\n=== Action Items ===\n");
        if (stats.getEscalationRate() > 10) {
            report.append("1. High escalation rate. Review escalation thresholds.\n");
        }
        if (pendingCount > 100) {
            report.append("2. Large backlog. Consider temporary measures.\n");
        }
        if (stats.getAverageResolutionTime() > 60) {
            report.append("3. Slow resolution. Optimize moderation workflow.\n");
        }

        return report.toString();
    }

    public String generateModeratorPerformanceReport(Long moderatorId, LocalDateTime start, LocalDateTime end) {
        // Генерация отчета по производительности конкретного модератора
        StringBuilder report = new StringBuilder();
        report.append(String.format("=== Moderator Performance Report ===\n\n"));
        report.append(String.format("Period: %s to %s\n\n",
                start.format(DATE_FORMATTER), end.format(DATE_FORMATTER)));

        // Здесь можно добавить статистику по конкретному модератору

        return report.toString();
    }
}