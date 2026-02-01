CREATE TABLE actor
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    date_of_birth date         NOT NULL,
    national      VARCHAR(100) NOT NULL,
    `role`        VARCHAR(100) NOT NULL,
    movie_id      BIGINT NULL,
    CONSTRAINT pk_actor PRIMARY KEY (id)
);

CREATE TABLE movie
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    title           VARCHAR(200)  NOT NULL,
    director        VARCHAR(100)  NOT NULL,
    runtime_minutes INT           NOT NULL,
    audience_rating VARCHAR(255)  NOT NULL,
    synopsis        VARCHAR(1000) NOT NULL,
    release_date    date          NOT NULL,
    status          VARCHAR(255)  NOT NULL,
    CONSTRAINT pk_movie PRIMARY KEY (id)
);

CREATE TABLE movie_genres
(
    movie_id BIGINT       NOT NULL,
    genre    VARCHAR(255) NOT NULL
);

ALTER TABLE actor
    ADD CONSTRAINT FK_ACTOR_ON_MOVIE FOREIGN KEY (movie_id) REFERENCES movie (id);

ALTER TABLE movie_genres
    ADD CONSTRAINT fk_movie_genres_on_movie FOREIGN KEY (movie_id) REFERENCES movie (id);