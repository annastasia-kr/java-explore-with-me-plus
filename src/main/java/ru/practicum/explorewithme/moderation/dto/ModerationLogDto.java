package ru.practicum.explorewithme.moderation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationLogDto {
    private Long id;
    private String entityType;
    private Long entityId;
    private String entityTitle;

    private Long moderatorId;
    private String moderatorName;
    private String moderatorEmail;

    private String action;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String previousState;
    private String newState;
    private Integer priorityLevel;
    private Boolean requiresFollowup;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followupDate;

    private Boolean autoModerated;
    private String metadata;

    // Дополнительные поля для UI
    private String entityUrl;
    private String moderatorAvatar;
    private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
}