ALTER TABLE movie
    ADD COLUMN genres_json JSON NULL,
    ADD COLUMN casts_json JSON NULL;

UPDATE movie m
SET m.genres_json = COALESCE(
        (
            SELECT JSON_ARRAYAGG(mg.genre)
            FROM movie_genres mg
            WHERE mg.movie_id = m.id
        ),
        JSON_ARRAY()
                    );

UPDATE movie m
SET m.casts_json = COALESCE(
        (
            SELECT JSON_ARRAYAGG(
                           JSON_OBJECT(
                                   'name', ma.name,
                                   'dateOfBirth', DATE_FORMAT(ma.date_of_birth, '%Y-%m-%dT%H:%i:%sZ'),
                                   'national', ma.national,
                                   'role', ma.role
                           )
                       )
            FROM movie_actors ma
            WHERE ma.movie_id = m.id
        ),
        JSON_ARRAY()
                   );

ALTER TABLE movie
    MODIFY COLUMN genres_json JSON NOT NULL,
    MODIFY COLUMN casts_json JSON NOT NULL;

DROP TABLE movie_genres;
DROP TABLE movie_actors;
