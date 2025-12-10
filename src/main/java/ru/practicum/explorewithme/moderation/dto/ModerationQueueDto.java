package ru.practicum.explorewithme.moderation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationQueueDto {
    private Long id;
    private String entityType;
    private Long entityId;
    private String entityTitle;
    private String entityContent;

    private Long eventId;
    private String eventTitle;
    private Long commentId;
    private String commentText;

    private Integer priorityScore;
    private String status;
    private Long assignedTo;
    private String assignedToName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    private Integer flagsCount;
    private List<String> flagReasons;
    private List<String> autoFlagReasons;

    private String contentType; // "TEXT", "IMAGE", "VIDEO"
    private Boolean requiresHumanReview;
    private Integer estimatedReviewTime; // в минутах
}