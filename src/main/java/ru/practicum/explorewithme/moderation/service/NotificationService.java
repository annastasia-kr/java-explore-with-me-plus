package ru.practicum.explorewithme.moderation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.moderation.model.ModerationLog;
import ru.practicum.explorewithme.moderation.model.ModerationQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final ModerationSettingsService settingsService;

    public void sendHighPriorityNotification(ModerationLog logEntry) {
        log.info("Sending high priority notification for log entry {}", logEntry.getId());

        if (!settingsService.getSettings().getEmailNotificationsEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        String subject = String.format("[High Priority] Moderation Action Required - %s %d",
                logEntry.getEntityType(), logEntry.getEntityId());

        String message = String.format("""
                High priority moderation action required:
                
                Entity Type: %s
                Entity ID: %d
                Action: %s
                Priority Level: %d
                Reason: %s
                
                Please review this item promptly.
                """,
                logEntry.getEntityType(),
                logEntry.getEntityId(),
                logEntry.getAction(),
                logEntry.getPriorityLevel(),
                logEntry.getReason());

        sendEmail(settingsService.getSettings().getNotificationEmail(), subject, message);
    }

    public void sendEscalationNotification(ModerationQueue queueItem) {
        log.info("Sending escalation notification for queue item {}", queueItem.getId());

        if (!settingsService.getSettings().getEmailNotificationsEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        String subject = String.format("[Escalation] Stuck Moderation Item - %s %d",
                queueItem.getEntityType(), queueItem.getEntityId());

        String message = String.format("""
                Moderation item has been escalated due to inactivity:
                
                Queue ID: %d
                Entity Type: %s
                Entity ID: %d
                Priority Score: %d
                Status: %s
                Created: %s
                
                This item was assigned for more than 24 hours without resolution.
                """,
                queueItem.getId(),
                queueItem.getEntityType(),
                queueItem.getEntityId(),
                queueItem.getPriorityScore(),
                queueItem.getStatus(),
                queueItem.getCreatedAt());

        sendEmail(settingsService.getSettings().getNotificationEmail(), subject, message);
    }

    public void sendDailyReport(String toEmail, String reportContent) {
        log.info("Sending daily moderation report to {}", toEmail);

        String subject = "Daily Moderation Report - " + java.time.LocalDate.now();

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(reportContent);

        try {
            mailSender.send(email);
            log.debug("Daily report sent successfully");
        } catch (Exception e) {
            log.error("Failed to send daily report", e);
        }
    }

    private void sendEmail(String to, String subject, String text) {
        if (to == null || to.trim().isEmpty()) {
            log.warn("No recipient email specified");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
            log.debug("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public void sendSlackNotification(String message) {
        if (!settingsService.getSettings().getSlackNotificationsEnabled()) {
            log.debug("Slack notifications are disabled");
            return;
        }

        String webhookUrl = settingsService.getSettings().getSlackWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("Slack webhook URL not configured");
            return;
        }

        // Реализация отправки в Slack
        log.info("Sending Slack notification: {}", message);
        // Здесь должна быть реальная интеграция с Slack API
    }
}