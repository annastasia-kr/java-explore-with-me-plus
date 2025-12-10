package ru.practicum.explorewithme.moderation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.moderation.model.ModerationQueue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModerationQueueRepository extends JpaRepository<ModerationQueue, Long> {

    Optional<ModerationQueue> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<ModerationQueue> findByStatus(String status);

    List<ModerationQueue> findByAssignedTo(Long assignedTo);

    List<ModerationQueue> findByStatusAndAssignedTo(String status, Long assignedTo);

    Page<ModerationQueue> findByStatus(String status, Pageable pageable);

    @Query("SELECT q FROM ModerationQueue q WHERE " +
            "q.status = 'PENDING' " +
            "ORDER BY q.priorityScore DESC, q.createdAt ASC")
    Page<ModerationQueue> findPendingByPriority(Pageable pageable);

    @Query("SELECT COUNT(q) FROM ModerationQueue q WHERE q.status = 'PENDING'")
    Long countPending();

    @Query("SELECT COUNT(q) FROM ModerationQueue q WHERE q.status = 'IN_PROGRESS' AND q.assignedTo = :moderatorId")
    Long countInProgressByModerator(@Param("moderatorId") Long moderatorId);

    @Query("SELECT q FROM ModerationQueue q WHERE " +
            "(:status IS NULL OR q.status = :status) AND " +
            "(:assignedTo IS NULL OR q.assignedTo = :assignedTo) AND " +
            "(:entityType IS NULL OR q.entityType = :entityType) AND " +
            "(:priorityMin IS NULL OR q.priorityScore >= :priorityMin) AND " +
            "(:priorityMax IS NULL OR q.priorityScore <= :priorityMax) AND " +
            "(:createdAfter IS NULL OR q.createdAt >= :createdAfter) " +
            "ORDER BY q.priorityScore DESC, q.createdAt ASC")
    Page<ModerationQueue> findWithFilters(@Param("status") String status,
                                          @Param("assignedTo") Long assignedTo,
                                          @Param("entityType") String entityType,
                                          @Param("priorityMin") Integer priorityMin,
                                          @Param("priorityMax") Integer priorityMax,
                                          @Param("createdAfter") LocalDateTime createdAfter,
                                          Pageable pageable);

    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, q.assignedAt, q.resolvedAt)) " +
            "FROM ModerationQueue q WHERE q.resolvedAt IS NOT NULL AND q.assignedAt IS NOT NULL")
    Double getAverageResolutionTime();

    @Query("SELECT q.entityType, COUNT(q) FROM ModerationQueue q " +
            "WHERE q.status = 'PENDING' GROUP BY q.entityType")
    List<Object[]> getPendingCountByEntityType();
}