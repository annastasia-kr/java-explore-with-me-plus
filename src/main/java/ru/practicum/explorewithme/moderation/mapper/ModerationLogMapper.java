
package ru.practicum.explorewithme.moderation.mapper;

import org.mapstruct.*;
import ru.practicum.explorewithme.moderation.dto.ModerationQueueDto;
import ru.practicum.explorewithme.moderation.model.ModerationQueue;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ModerationQueueMapper {

    @Mapping(target = "entityTitle", expression = "java(getEntityTitle(queue))")
    @Mapping(target = "entityContent", expression = "java(getEntityContent(queue))")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "commentText", source = "comment.text")
    @Mapping(target = "flagReasons", expression = "java(parseFlagReasons(queue.getAutoFlagReasons()))")
    @Mapping(target = "requiresHumanReview", expression = "java(determineIfRequiresHumanReview(queue))")
    @Mapping(target = "estimatedReviewTime", expression = "java(estimateReviewTime(queue))")
    ModerationQueueDto toDto(ModerationQueue queue);

    default String getEntityTitle(ModerationQueue queue) {
        if (queue.getEvent() != null) {
            return queue.getEvent().getTitle();
        } else if (queue.getComment() != null) {
            return "Comment #" + queue.getComment().getId();
        }
        return "Entity #" + queue.getEntityId();
    }

    default String getEntityContent(ModerationQueue queue) {
        if (queue.getComment() != null) {
            String text = queue.getComment().getText();
            return text.length() > 100 ? text.substring(0, 100) + "..." : text;
        } else if (queue.getEvent() != null) {
            return queue.getEvent().getAnnotation();
        }
        return "";
    }

    default List<String> parseFlagReasons(String autoFlagReasons) {
        if (autoFlagReasons == null || autoFlagReasons.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(autoFlagReasons.split(","));
    }

    default Boolean determineIfRequiresHumanReview(ModerationQueue queue) {
        return queue.getPriorityScore() >= 3 || queue.getFlagsCount() > 0;
    }

    default Integer estimateReviewTime(ModerationQueue queue) {
        if (queue.getPriorityScore() >= 4) return 10;
        if (queue.getPriorityScore() >= 3) return 5;
        if (queue.getPriorityScore() >= 2) return 3;
        return 1;
    }
}

// ModerationRuleMapper.java
package ru.practicum.explorewithme.moderation.mapper;

import org.mapstruct.*;
import ru.practicum.explorewithme.moderation.dto.ModerationRuleDto;
import ru.practicum.explorewithme.moderation.model.ModerationRule;

@Mapper(componentModel = "spring")
public interface ModerationRuleMapper {

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.name")
    @Mapping(target = "hitCount", expression = "java(0L)") // Заглушка, нужно вычислять
    @Mapping(target = "lastHitAt", expression = "java(null)") // Заглушка
    @Mapping(target = "effectiveness", expression = "java(0.0)") // Заглушка
    ModerationRuleDto toDto(ModerationRule rule);
}