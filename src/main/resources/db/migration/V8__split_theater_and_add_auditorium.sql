ALTER TABLE theater
    DROP COLUMN floor,
    DROP COLUMN theater_type,
    DROP COLUMN seats,
    DROP COLUMN row_count,
    DROP COLUMN column_count;

CREATE TABLE auditorium
(
    auditorium_id   BIGINT AUTO_INCREMENT NOT NULL,
    theater_id      BIGINT      NOT NULL,
    name            VARCHAR(50) NOT NULL,
    floor           INT         NOT NULL,
    auditorium_type VARCHAR(50) NOT NULL,
    is_active       BIT(1)      NOT NULL,
    seats           JSON        NOT NULL,
    row_count       INT         NOT NULL,
    column_count    INT         NOT NULL,
    CONSTRAINT pk_auditorium PRIMARY KEY (auditorium_id)
);

CREATE INDEX idx_auditorium_theater_id ON auditorium (theater_id);

ALTER TABLE auditorium
    ADD CONSTRAINT uc_auditorium_theater_name UNIQUE (theater_id, name);

ALTER TABLE auditorium
    ADD CONSTRAINT fk_auditorium_theater
        FOREIGN KEY (theater_id) REFERENCES theater (theater_id);
