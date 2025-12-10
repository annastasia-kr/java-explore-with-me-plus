package ru.practicum.explorewithme.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentAnalysisResult {
    private Long entityId;
    private String entityType;
    private String content;

    private Double toxicityScore; // 0-1
    private Double spamScore; // 0-1
    private Double sentimentScore; // -1 to 1

    private List<String> flaggedKeywords;
    private List<String> suspiciousPatterns;
    private Map<String, Double> categoryScores; // категории: SPAM, TOXIC, ADULT, etc.

    private Boolean requiresModeration;
    private String recommendedAction;
    private Integer priorityLevel;

    private List<String> explanations; // объяснения почему контент помечен
    private String analysisSummary;
}