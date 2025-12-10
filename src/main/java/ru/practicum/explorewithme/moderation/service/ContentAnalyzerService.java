package ru.practicum.explorewithme.moderation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.moderation.dto.ContentAnalysisResult;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ContentAnalyzerService {

    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
            "buy now", "click here", "limited offer", "discount", "free",
            "make money", "work from home", "earn cash", "investment",
            "guaranteed", "risk-free", "act now", "don't miss"
    );

    private static final List<String> TOXIC_KEYWORDS = Arrays.asList(
            "idiot", "stupid", "hate", "kill", "die", "worthless",
            "ugly", "fat", "retard", "moron", "bastard"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://(?:[\\w\\d-]+\\.)+[\\w\\d]{2,}(?:/[\\w\\d-./?%&=]*)?"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\+?[\\d\\s-]{10,}"
    );

    public ContentAnalysisResult analyzeContent(String content, String entityType, Long entityId) {
        log.debug("Analyzing content for {} {}: {}", entityType, entityId, content.substring(0, Math.min(content.length(), 50)));

        content = content.toLowerCase();

        // Анализ на спам
        double spamScore = calculateSpamScore(content);

        // Анализ на токсичность
        double toxicityScore = calculateToxicityScore(content);

        // Анализ настроения (простой)
        double sentimentScore = calculateSentimentScore(content);

        // Поиск ключевых слов
        List<String> flaggedKeywords = findFlaggedKeywords(content);

        // Поиск подозрительных паттернов
        List<String> suspiciousPatterns = findSuspiciousPatterns(content);

        // Определение категорий
        Map<String, Double> categoryScores = calculateCategoryScores(content);

        // Определение необходимости модерации
        boolean requiresModeration = determineIfRequiresModeration(
                spamScore, toxicityScore, flaggedKeywords);

        // Рекомендуемое действие
        String recommendedAction = determineRecommendedAction(
                spamScore, toxicityScore, requiresModeration);

        // Уровень приоритета
        int priorityLevel = calculatePriorityLevel(
                spamScore, toxicityScore, flaggedKeywords.size());

        return ContentAnalysisResult.builder()
                .entityId(entityId)
                .entityType(entityType)
                .content(content)
                .toxicityScore(toxicityScore)
                .spamScore(spamScore)
                .sentimentScore(sentimentScore)
                .flaggedKeywords(flaggedKeywords)
                .suspiciousPatterns(suspiciousPatterns)
                .categoryScores(categoryScores)
                .requiresModeration(requiresModeration)
                .recommendedAction(recommendedAction)
                .priorityLevel(priorityLevel)
                .explanations(generateExplanations(
                        spamScore, toxicityScore, flaggedKeywords, suspiciousPatterns))
                .analysisSummary(generateAnalysisSummary(
                        spamScore, toxicityScore, requiresModeration))
                .build();
    }

    private double calculateSpamScore(String content) {
        double score = 0.0;

        // Проверка на спам-ключевые слова
        for (String keyword : SPAM_KEYWORDS) {
            if (content.contains(keyword)) {
                score += 0.1;
            }
        }

        // Проверка на URL
        if (URL_PATTERN.matcher(content).find()) {
            score += 0.3;
        }

        // Проверка на email
        if (EMAIL_PATTERN.matcher(content).find()) {
            score += 0.2;
        }

        // Проверка на телефон
        if (PHONE_PATTERN.matcher(content).find()) {
            score += 0.2;
        }

        // Проверка на повторяющиеся слова
        score += calculateRepetitionScore(content);

        return Math.min(score, 1.0);
    }

    private double calculateToxicityScore(String content) {
        double score = 0.0;

        for (String keyword : TOXIC_KEYWORDS) {
            if (content.contains(keyword)) {
                score += 0.15;
            }
        }

        // Проверка на CAPS LOCK
        if (content.length() > 10) {
            long uppercaseCount = content.chars().filter(Character::isUpperCase).count();
            if ((double) uppercaseCount / content.length() > 0.7) {
                score += 0.2;
            }
        }

        // Проверка на множественные восклицательные знаки
        if (content.contains("!!!") || content.contains("???")) {
            score += 0.1;
        }

        return Math.min(score, 1.0);
    }

    private double calculateSentimentScore(String content) {
        // Простой анализ настроения на основе ключевых слов
        List<String> positiveWords = Arrays.asList(
                "good", "great", "excellent", "awesome", "love", "happy",
                "nice", "perfect", "best", "thanks", "thank you"
        );

        List<String> negativeWords = Arrays.asList(
                "bad", "terrible", "awful", "hate", "angry", "sad",
                "poor", "worst", "disappoint", "fail", "problem"
        );

        double positiveScore = 0.0;
        double negativeScore = 0.0;

        for (String word : positiveWords) {
            if (content.contains(word)) {
                positiveScore += 0.1;
            }
        }

        for (String word : negativeWords) {
            if (content.contains(word)) {
                negativeScore += 0.1;
            }
        }

        // Нормализация
        double total = positiveScore + negativeScore;
        if (total == 0) return 0.0;

        return (positiveScore - negativeScore) / total;
    }

    private double calculateRepetitionScore(String content) {
        String[] words = content.split("\\s+");
        if (words.length < 3) return 0.0;

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }

        int repeatedWords = 0;
        for (int count : wordCount.values()) {
            if (count > 3) {
                repeatedWords++;
            }
        }

        return Math.min(repeatedWords * 0.1, 0.3);
    }

    private List<String> findFlaggedKeywords(String content) {
        List<String> flagged = new ArrayList<>();

        List<String> allKeywords = new ArrayList<>();
        allKeywords.addAll(SPAM_KEYWORDS);
        allKeywords.addAll(TOXIC_KEYWORDS);

        for (String keyword : allKeywords) {
            if (content.contains(keyword)) {
                flagged.add(keyword);
            }
        }

        return flagged;
    }

    private List<String> findSuspiciousPatterns(String content) {
        List<String> patterns = new ArrayList<>();

        if (URL_PATTERN.matcher(content).find()) {
            patterns.add("URL_DETECTED");
        }

        if (EMAIL_PATTERN.matcher(content).find()) {
            patterns.add("EMAIL_DETECTED");
        }

        if (PHONE_PATTERN.matcher(content).find()) {
            patterns.add("PHONE_DETECTED");
        }

        // Проверка на подозрительные домены
        if (content.contains(".ru/") || content.contains(".tk/") || content.contains(".ml/")) {
            patterns.add("SUSPICIOUS_DOMAIN");
        }

        return patterns;
    }

    private Map<String, Double> calculateCategoryScores(String content) {
        Map<String, Double> scores = new HashMap<>();

        scores.put("SPAM", calculateSpamScore(content));
        scores.put("TOXIC", calculateToxicityScore(content));
        scores.put("ADULT", calculateAdultScore(content));
        scores.put("COMMERCIAL", calculateCommercialScore(content));

        return scores;
    }

    private double calculateAdultScore(String content) {
        List<String> adultKeywords = Arrays.asList(
                "sex", "porn", "adult", "explicit", "nsfw",
                "nude", "naked", "xxx", "18+", "adult only"
        );

        double score = 0.0;
        for (String keyword : adultKeywords) {
            if (content.contains(keyword)) {
                score += 0.2;
            }
        }

        return Math.min(score, 1.0);
    }

    private double calculateCommercialScore(String content) {
        List<String> commercialKeywords = Arrays.asList(
                "price", "cost", "sale", "discount", "offer",
                "buy", "purchase", "order", "shop", "store"
        );

        double score = 0.0;
        for (String keyword : commercialKeywords) {
            if (content.contains(keyword)) {
                score += 0.1;
            }
        }

        return Math.min(score, 1.0);
    }

    private boolean determineIfRequiresModeration(double spamScore, double toxicityScore,
                                                  List<String> flaggedKeywords) {
        if (spamScore > 0.5) return true;
        if (toxicityScore > 0.4) return true;
        if (!flaggedKeywords.isEmpty()) return true;

        return false;
    }

    private String determineRecommendedAction(double spamScore, double toxicityScore,
                                              boolean requiresModeration) {
        if (!requiresModeration) {
            return "APPROVE";
        }

        if (spamScore > 0.7 || toxicityScore > 0.6) {
            return "AUTO_REJECT";
        }

        return "REVIEW";
    }

    private int calculatePriorityLevel(double spamScore, double toxicityScore, int flaggedCount) {
        int level = 1;

        if (spamScore > 0.7) level += 2;
        if (toxicityScore > 0.6) level += 3;
        if (flaggedCount > 2) level += 1;

        return Math.min(level, 5);
    }

    private List<String> generateExplanations(double spamScore, double toxicityScore,
                                              List<String> flaggedKeywords,
                                              List<String> suspiciousPatterns) {
        List<String> explanations = new ArrayList<>();

        if (spamScore > 0.3) {
            explanations.add(String.format("Spam score: %.2f", spamScore));
        }

        if (toxicityScore > 0.3) {
            explanations.add(String.format("Toxicity score: %.2f", toxicityScore));
        }

        if (!flaggedKeywords.isEmpty()) {
            explanations.add("Flagged keywords: " + String.join(", ", flaggedKeywords));
        }

        if (!suspiciousPatterns.isEmpty()) {
            explanations.add("Suspicious patterns: " + String.join(", ", suspiciousPatterns));
        }

        return explanations;
    }

    private String generateAnalysisSummary(double spamScore, double toxicityScore,
                                           boolean requiresModeration) {
        if (!requiresModeration) {
            return "Content appears safe";
        }

        List<String> issues = new ArrayList<>();

        if (spamScore > 0.5) {
            issues.add("high spam probability");
        }

        if (toxicityScore > 0.4) {
            issues.add("potential toxicity");
        }

        if (issues.isEmpty()) {
            return "Content requires review";
        }

        return "Content has " + String.join(" and ", issues);
    }
}