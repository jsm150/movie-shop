ALTER TABLE screening
    CHANGE theater_id auditorium_id BIGINT NOT NULL;

DROP INDEX idx_screening_theater_end_start ON screening;

CREATE INDEX idx_screening_auditorium_end_start ON screening (auditorium_id, end_time, start_time);
