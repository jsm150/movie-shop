CREATE TABLE operator_account
(
    operator_id   BIGINT AUTO_INCREMENT NOT NULL,
    login_id      VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    status        VARCHAR(30) NOT NULL,
    CONSTRAINT pk_operator_account PRIMARY KEY (operator_id)
);

ALTER TABLE operator_account
    ADD CONSTRAINT uk_operator_account_login_id UNIQUE (login_id);
