-- Создание таблиц для расширенной модерации
CREATE TABLE moderation_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    moderator_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    reason VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    previous_state VARCHAR(50),
    new_state VARCHAR(50),
    priority_level INTEGER DEFAULT 1,
    requires_followup BOOLEAN DEFAULT false,
    followup_date TIMESTAMP,
    auto_moderated BOOLEAN DEFAULT false,
    metadata TEXT,
    INDEX idx_moderation_logs_entity (entity_type, entity_id),
    INDEX idx_moderation_logs_created (created_at),
    INDEX idx_moderation_logs_moderator (moderator_id)
);

CREATE TABLE moderation_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    entity_type VARCHAR(50) NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    condition_value VARCHAR(500),
    action VARCHAR(50) NOT NULL,
    priority_level INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    weight INTEGER DEFAULT 1,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    INDEX idx_moderation_rules_active (is_active),
    INDEX idx_moderation_rules_entity (entity_type)
);

CREATE TABLE moderation_queue (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    event_id BIGINT REFERENCES events(id),
    comment_id BIGINT REFERENCES comments(id),
    priority_score INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_to BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMP,
    resolved_at TIMESTAMP,
    flags_count INTEGER DEFAULT 0,
    auto_flag_reasons TEXT,
    INDEX idx_moderation_queue_status (status),
    INDEX idx_moderation_queue_priority (priority_score DESC),
    INDEX idx_moderation_queue_assigned (assigned_to),
    INDEX idx_moderation_queue_entity (entity_type, entity_id),
    UNIQUE (entity_type, entity_id)
);

CREATE TABLE moderation_settings (
    id BIGINT PRIMARY KEY DEFAULT 1,
    auto_moderation_enabled BOOLEAN DEFAULT true,
    email_notifications_enabled BOOLEAN DEFAULT true,
    slack_notifications_enabled BOOLEAN DEFAULT false,
    auto_approve_threshold INTEGER DEFAULT 7,
    auto_reject_threshold INTEGER DEFAULT 0,
    escalation_threshold INTEGER DEFAULT 24,
    max_queue_size INTEGER DEFAULT 1000,
    max_assignments_per_moderator INTEGER DEFAULT 5,
    default_timezone VARCHAR(50) DEFAULT 'UTC',
    notification_email VARCHAR(255),
    slack_webhook_url VARCHAR(500),
    enable_sentiment_analysis BOOLEAN DEFAULT true,
    enable_keyword_filtering BOOLEAN DEFAULT true,
    enable_pattern_detection BOOLEAN DEFAULT true,
    review_time_limit INTEGER DEFAULT 60,
    enable_priority_scoring BOOLEAN DEFAULT true,
    enable_quality_control BOOLEAN DEFAULT true
);

-- Вставка настроек по умолчанию
INSERT INTO moderation_settings (id) VALUES (1);