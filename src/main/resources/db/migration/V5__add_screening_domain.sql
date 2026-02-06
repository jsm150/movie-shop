CREATE TABLE screening
(
    screening_id   BIGINT AUTO_INCREMENT NOT NULL,
    movie_id       BIGINT                NOT NULL,
    theater_id     BIGINT                NOT NULL,
    start_time     datetime              NOT NULL,
    end_time       datetime              NOT NULL,
    sales_start_at datetime              NOT NULL,
    sales_end_at   datetime              NOT NULL,
    status         VARCHAR(30)           NOT NULL,
    canceled_at    datetime              NULL,
    cancel_reason  VARCHAR(200)          NULL,
    CONSTRAINT pk_screening PRIMARY KEY (screening_id)
);

CREATE INDEX idx_screening_theater_start ON screening (theater_id, start_time);
CREATE INDEX idx_screening_movie_start ON screening (movie_id, start_time);
