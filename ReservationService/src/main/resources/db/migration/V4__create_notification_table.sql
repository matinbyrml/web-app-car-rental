DROP TABLE IF EXISTS user_notifications;

CREATE TABLE IF NOT EXISTS user_notifications (
                                    id           BIGSERIAL PRIMARY KEY,
                                    user_id      BIGINT NOT NULL,
                                    type         VARCHAR(40) NOT NULL,
                                    status       VARCHAR(20) NOT NULL,
                                    title        VARCHAR(120) NOT NULL,
                                    body         TEXT NOT NULL,
                                    created_by   VARCHAR(80) NOT NULL,
                                    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_user_notifications_user_status_created
    ON user_notifications (user_id, status, created_at DESC);
