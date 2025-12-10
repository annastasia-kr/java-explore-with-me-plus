package ru.practicum.explorewithme.moderation.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "moderation_rules")
public class ModerationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType; // "CONTAINS", "REGEX", "SENTIMENT", "VOLUME"

    @Column(name = "condition_value", length = 500)
    private String conditionValue;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // "FLAG", "AUTO_REJECT", "AUTO_APPROVE", "ESCALATE"

    @Column(name = "priority_level")
    private Integer priorityLevel;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "weight")
    private Integer weight = 1;

    @Column(name = "description", length = 1000)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @ToString.Exclude
    private ru.practicum.explorewithme.user.model.User createdBy;
}