package ru.practicum.explorewithme.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationSettings {
    private Boolean autoModerationEnabled;
    private Boolean emailNotificationsEnabled;
    private Boolean slackNotificationsEnabled;

    private Integer autoApproveThreshold; // порог для автоодобрения
    private Integer autoRejectThreshold; // порог для автоотклонения
    private Integer escalationThreshold; // порог для эскалации

    private Integer maxQueueSize;
    private Integer maxAssignmentsPerModerator;

    private String defaultTimezone;
    private String notificationEmail;
    private String slackWebhookUrl;

    private Boolean enableSentimentAnalysis;
    private Boolean enableKeywordFiltering;
    private Boolean enablePatternDetection;

    private Integer reviewTimeLimit; // лимит времени на ревью в минутах
    private Boolean enablePriorityScoring;
    private Boolean enableQualityControl;
}