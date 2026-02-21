DROP INDEX idx_screening_theater_start ON screening;

CREATE INDEX idx_screening_theater_end_start ON screening (theater_id, end_time, start_time);
