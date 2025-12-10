package ru.practicum.explorewithme.moderation.model;

import lombok.*;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.comment.model.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "moderation_queue")
public class ModerationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    @ToString.Exclude
    private Comment comment;

    @Column(name = "priority_score", nullable = false)
    private Integer priorityScore;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // "PENDING", "IN_PROGRESS", "RESOLVED", "ESCALATED"

    @Column(name = "assigned_to")
    private Long assignedTo; // ID модератора

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "flags_count")
    private Integer flagsCount = 0;

    @Column(name = "auto_flag_reasons", columnDefinition = "TEXT")
    private String autoFlagReasons; // JSON с причинами автофлагов

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (priorityScore == null) {
            priorityScore = 1;
        }
    }
}