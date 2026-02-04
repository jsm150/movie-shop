CREATE TABLE theater
(
    theater_id   BIGINT AUTO_INCREMENT NOT NULL,
    name         VARCHAR(50) NOT NULL,
    floor        INT NOT NULL,
    theater_type VARCHAR(255) NOT NULL,
    is_active    BIT(1) NOT NULL,
    seats        JSON NOT NULL,
    row_count    INT    NOT NULL,
    column_count INT    NOT NULL,
    CONSTRAINT pk_theater PRIMARY KEY (theater_id)
);

ALTER TABLE theater
    ADD CONSTRAINT uc_theater_name UNIQUE (name);