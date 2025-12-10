package ru.practicum.explorewithme.moderation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.moderation.model.ModerationLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, Long> {

    List<ModerationLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<ModerationLog> findByModeratorId(Long moderatorId);

    List<ModerationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<ModerationLog> findByActionAndCreatedAtBetween(String action, LocalDateTime start, LocalDateTime end);

    List<ModerationLog> findByAutoModeratedTrue();

    Page<ModerationLog> findByRequiresFollowupTrue(Pageable pageable);

    @Query("SELECT COUNT(ml) FROM ModerationLog ml WHERE ml.createdAt >= :start AND ml.createdAt <= :end")
    Long countByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT ml.entityType, COUNT(ml) FROM ModerationLog ml " +
            "WHERE ml.createdAt >= :start AND ml.createdAt <= :end " +
            "GROUP BY ml.entityType")
    List<Object[]> countByEntityType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT ml FROM ModerationLog ml " +
            "WHERE (:entityType IS NULL OR ml.entityType = :entityType) " +
            "AND (:entityId IS NULL OR ml.entityId = :entityId) " +
            "AND (:moderatorId IS NULL OR ml.moderator.id = :moderatorId) " +
            "AND (:action IS NULL OR ml.action = :action) " +
            "AND (:start IS NULL OR ml.createdAt >= :start) " +
            "AND (:end IS NULL OR ml.createdAt <= :end) " +
            "AND (:autoModerated IS NULL OR ml.autoModerated = :autoModerated) " +
            "ORDER BY ml.createdAt DESC")
    Page<ModerationLog> findWithFilters(@Param("entityType") String entityType,
                                        @Param("entityId") Long entityId,
                                        @Param("moderatorId") Long moderatorId,
                                        @Param("action") String action,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("autoModerated") Boolean autoModerated,
                                        Pageable pageable);

    @Query("SELECT ml.moderator.name, COUNT(ml) FROM ModerationLog ml " +
            "WHERE ml.createdAt >= :start AND ml.createdAt <= :end " +
            "AND ml.moderator IS NOT NULL " +
            "GROUP BY ml.moderator.name " +
            "ORDER BY COUNT(ml) DESC")
    List<Object[]> getTopModerators(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}