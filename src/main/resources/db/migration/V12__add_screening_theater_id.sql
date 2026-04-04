ALTER TABLE screening
    ADD COLUMN theater_id BIGINT NULL AFTER auditorium_id;

UPDATE screening s
    JOIN auditorium a ON a.auditorium_id = s.auditorium_id
SET s.theater_id = a.theater_id;

ALTER TABLE screening
    MODIFY theater_id BIGINT NOT NULL;
