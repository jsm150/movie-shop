CREATE TABLE movie_genre
(
    movie_id BIGINT      NOT NULL,
    genre    VARCHAR(50) NOT NULL
);

CREATE TABLE movie_cast
(
    movie_id      BIGINT       NOT NULL,
    cast_order    INT          NOT NULL,
    name          VARCHAR(100) NOT NULL,
    date_of_birth DATETIME     NOT NULL,
    national      VARCHAR(100) NOT NULL,
    `role`        VARCHAR(100) NOT NULL,
    CONSTRAINT pk_movie_cast PRIMARY KEY (movie_id, cast_order)
);

INSERT INTO movie_genre (
    movie_id,
    genre
)
SELECT m.id,
       jt.genre
FROM movie m
         JOIN JSON_TABLE(
        m.genres_json,
        '$[*]' COLUMNS (
            genre VARCHAR(50) PATH '$'
            )
              ) AS jt ON TRUE;

INSERT INTO movie_cast (
    movie_id,
    cast_order,
    name,
    date_of_birth,
    national,
    `role`
)
SELECT m.id,
       jt.cast_order - 1,
       jt.name,
       CASE
           WHEN jt.date_of_birth REGEXP '^-?[0-9]+(\\.[0-9]+)?$'
               THEN FROM_UNIXTIME(CAST(jt.date_of_birth AS DECIMAL(20, 6)))
           ELSE STR_TO_DATE(
               SUBSTRING(REPLACE(jt.date_of_birth, 'T', ' '), 1, 19),
               '%Y-%m-%d %H:%i:%s'
                )
           END,
       jt.national,
       jt.role_name
FROM movie m
         JOIN JSON_TABLE(
        m.casts_json,
        '$[*]' COLUMNS (
            cast_order FOR ORDINALITY,
            name VARCHAR(100) PATH '$.name',
            date_of_birth VARCHAR(40) PATH '$.dateOfBirth',
            national VARCHAR(100) PATH '$.national',
            role_name VARCHAR(100) PATH '$.role'
            )
              ) AS jt ON TRUE;

CREATE INDEX idx_movie_genre_genre
    ON movie_genre (genre);

ALTER TABLE movie_genre
    ADD CONSTRAINT fk_movie_genre_movie
        FOREIGN KEY (movie_id) REFERENCES movie (id)
            ON DELETE CASCADE;

ALTER TABLE movie_cast
    ADD CONSTRAINT fk_movie_cast_movie
        FOREIGN KEY (movie_id) REFERENCES movie (id)
            ON DELETE CASCADE;

ALTER TABLE movie
    DROP COLUMN genres_json,
    DROP COLUMN casts_json;
