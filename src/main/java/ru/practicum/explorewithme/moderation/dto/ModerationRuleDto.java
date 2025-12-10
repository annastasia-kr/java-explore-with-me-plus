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
public class ModerationRuleDto {
    private Long id;
    private String ruleName;
    private String entityType;
    private String conditionType;
    private String conditionValue;
    private String action;
    private Integer priorityLevel;
    private Boolean isActive;
    private Integer weight;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long createdById;
    private String createdByName;

    private Long hitCount; // сколько раз сработало правило
    private Long lastHitAt;
    private Double effectiveness; // процент правильных срабатываний
}