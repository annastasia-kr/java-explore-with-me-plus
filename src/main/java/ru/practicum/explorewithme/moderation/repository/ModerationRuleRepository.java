package ru.practicum.explorewithme.moderation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.moderation.model.ModerationRule;

import java.util.List;

@Repository
public interface ModerationRuleRepository extends JpaRepository<ModerationRule, Long> {

    List<ModerationRule> findByEntityTypeAndIsActiveTrue(String entityType);

    List<ModerationRule> findByIsActiveTrue();

    List<ModerationRule> findByConditionType(String conditionType);

    @Query("SELECT r FROM ModerationRule r WHERE " +
            "r.isActive = true AND " +
            "(:entityType IS NULL OR r.entityType = :entityType) AND " +
            "(:action IS NULL OR r.action = :action)")
    List<ModerationRule> findActiveRules(@Param("entityType") String entityType,
                                         @Param("action") String action);

    boolean existsByRuleName(String ruleName);
}