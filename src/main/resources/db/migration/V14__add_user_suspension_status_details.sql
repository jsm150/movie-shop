ALTER TABLE user_account
    ADD COLUMN suspension_reason_code VARCHAR(50) NULL,
    ADD COLUMN suspension_reason_memo VARCHAR(500) NULL,
    ADD COLUMN suspended_by_operator_id BIGINT NULL,
    ADD COLUMN suspended_at datetime NULL;

CREATE INDEX idx_user_account_status_suspended_at
    ON user_account (status, suspended_at);
