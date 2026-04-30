CREATE TABLE user_account
(
    user_id                BIGINT AUTO_INCREMENT NOT NULL,
    oauth_provider         VARCHAR(30) NOT NULL,
    oauth_provider_user_id VARCHAR(255) NOT NULL,
    email                  VARCHAR(255) NULL,
    name                   VARCHAR(100) NOT NULL,
    status                 VARCHAR(30) NOT NULL,
    CONSTRAINT pk_user_account PRIMARY KEY (user_id)
);

ALTER TABLE user_account
    ADD CONSTRAINT uk_user_account_oauth_identity UNIQUE (oauth_provider, oauth_provider_user_id);
