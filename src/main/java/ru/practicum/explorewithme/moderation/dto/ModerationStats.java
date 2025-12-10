package ru.practicum.explorewithme.moderation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationStats {
    private Long totalModerations;
    private Long pendingItems;
    private Long resolvedItems;
    private Long escalatedItems;
    private Long commentModerations;
    private Long eventModerations;
    private Long userModerations;

    private Map<String, Long> actionsByType;
    private Map<String, Long> moderationsByUser;
    private Map<String, Long> entitiesByType;

    private Double averageResolutionTime; // в минутах
    private Double escalationRate; // процент

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
}