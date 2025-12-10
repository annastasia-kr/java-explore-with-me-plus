package ru.practicum.explorewithme.moderation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.moderation.dto.ModerationSettings;
import ru.practicum.explorewithme.moderation.model.ModerationSettingsEntity;
import ru.practicum.explorewithme.moderation.repository.ModerationSettingsRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationSettingsService {

    private final ModerationSettingsRepository settingsRepository;

    private static final Long DEFAULT_SETTINGS_ID = 1L;

    @Transactional(readOnly = true)
    public ModerationSettings getSettings() {
        Optional<ModerationSettingsEntity> entityOpt = settingsRepository.findById(DEFAULT_SETTINGS_ID);

        if (entityOpt.isPresent()) {
            return mapToDto(entityOpt.get());
        }

        // Возвращаем настройки по умолчанию
        return getDefaultSettings();
    }

    @Transactional
    public ModerationSettings updateSettings(ModerationSettings settingsDto) {
        ModerationSettingsEntity entity = settingsRepository.findById(DEFAULT_SETTINGS_ID)
                .orElse(ModerationSettingsEntity.builder().id(DEFAULT_SETTINGS_ID).build());

        updateEntityFromDto(entity, settingsDto);

        ModerationSettingsEntity saved = settingsRepository.save(entity);
        log.info("Moderation settings updated");

        return mapToDto(saved);
    }

    private ModerationSettings getDefaultSettings() {
        return ModerationSettings.builder()
                .autoModerationEnabled(true)
                .emailNotificationsEnabled(true)
                .slackNotificationsEnabled(false)
                .autoApproveThreshold(7) // дней
                .autoRejectThreshold(0) // всегда отправлять на ревью
                .escalationThreshold(24) // часов
                .maxQueueSize(1000)
                .maxAssignmentsPerModerator(5)
                .defaultTimezone("UTC")
                .notificationEmail("admin@example.com")
                .slackWebhookUrl("")
                .enableSentimentAnalysis(true)
                .enableKeywordFiltering(true)
                .enablePatternDetection(true)
                .reviewTimeLimit(60) // минут
                .enablePriorityScoring(true)
                .enableQualityControl(true)
                .build();
    }

    private ModerationSettings mapToDto(ModerationSettingsEntity entity) {
        return ModerationSettings.builder()
                .autoModerationEnabled(entity.getAutoModerationEnabled())
                .emailNotificationsEnabled(entity.getEmailNotificationsEnabled())
                .slackNotificationsEnabled(entity.getSlackNotificationsEnabled())
                .autoApproveThreshold(entity.getAutoApproveThreshold())
                .autoRejectThreshold(entity.getAutoRejectThreshold())
                .escalationThreshold(entity.getEscalationThreshold())
                .maxQueueSize(entity.getMaxQueueSize())
                .maxAssignmentsPerModerator(entity.getMaxAssignmentsPerModerator())
                .defaultTimezone(entity.getDefaultTimezone())
                .notificationEmail(entity.getNotificationEmail())
                .slackWebhookUrl(entity.getSlackWebhookUrl())
                .enableSentimentAnalysis(entity.getEnableSentimentAnalysis())
                .enableKeywordFiltering(entity.getEnableKeywordFiltering())
                .enablePatternDetection(entity.getEnablePatternDetection())
                .reviewTimeLimit(entity.getReviewTimeLimit())
                .enablePriorityScoring(entity.getEnablePriorityScoring())
                .enableQualityControl(entity.getEnableQualityControl())
                .build();
    }

    private void updateEntityFromDto(ModerationSettingsEntity entity, ModerationSettings dto) {
        entity.setAutoModerationEnabled(dto.getAutoModerationEnabled());
        entity.setEmailNotificationsEnabled(dto.getEmailNotificationsEnabled());
        entity.setSlackNotificationsEnabled(dto.getSlackNotificationsEnabled());
        entity.setAutoApproveThreshold(dto.getAutoApproveThreshold());
        entity.setAutoRejectThreshold(dto.getAutoRejectThreshold());
        entity.setEscalationThreshold(dto.getEscalationThreshold());
        entity.setMaxQueueSize(dto.getMaxQueueSize());
        entity.setMaxAssignmentsPerModerator(dto.getMaxAssignmentsPerModerator());
        entity.setDefaultTimezone(dto.getDefaultTimezone());
        entity.setNotificationEmail(dto.getNotificationEmail());
        entity.setSlackWebhookUrl(dto.getSlackWebhookUrl());
        entity.setEnableSentimentAnalysis(dto.getEnableSentimentAnalysis());
        entity.setEnableKeywordFiltering(dto.getEnableKeywordFiltering());
        entity.setEnablePatternDetection(dto.getEnablePatternDetection());
        entity.setReviewTimeLimit(dto.getReviewTimeLimit());
        entity.setEnablePriorityScoring(dto.getEnablePriorityScoring());
        entity.setEnableQualityControl(dto.getEnableQualityControl());
    }
}