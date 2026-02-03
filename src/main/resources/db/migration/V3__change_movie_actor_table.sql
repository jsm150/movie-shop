ALTER TABLE actor
DROP
FOREIGN KEY FK_ACTOR_ON_MOVIE;

CREATE TABLE movie_actors
(
    movie_id      BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    date_of_birth datetime     NOT NULL,
    national      VARCHAR(100) NOT NULL,
    `role`        VARCHAR(100) NOT NULL
);

ALTER TABLE movie_actors
    ADD CONSTRAINT fk_movie_actors_on_movie FOREIGN KEY (movie_id) REFERENCES movie (id);

DROP TABLE actor;

ALTER TABLE movie
DROP
COLUMN release_date;

ALTER TABLE movie
    ADD release_date datetime NOT NULL;