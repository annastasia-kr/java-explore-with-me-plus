package ru.practicum.explorewithme.moderation.model;

import lombok.*;
import ru.practicum.explorewithme.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "moderation_logs")
public class ModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // "EVENT", "COMMENT", "USER"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    @ToString.Exclude
    private User moderator;

    @Column(nullable = false, length = 50)
    private String action; // "APPROVE", "REJECT", "FLAG", "BAN", "WARNING", "ESCALATE"

    @Column(length = 2000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "previous_state", length = 50)
    private String previousState;

    @Column(name = "new_state", length = 50)
    private String newState;

    @Column(name = "priority_level")
    private Integer priorityLevel; // 1-5, где 5 - наивысший

    @Column(name = "requires_followup")
    private Boolean requiresFollowup = false;

    @Column(name = "followup_date")
    private LocalDateTime followupDate;

    @Column(name = "auto_moderated")
    private Boolean autoModerated = false;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON с дополнительными данными

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (priorityLevel == null) {
            priorityLevel = 1;
        }
    }
}