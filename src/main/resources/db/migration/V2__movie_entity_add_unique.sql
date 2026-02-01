ALTER TABLE movie
    ADD CONSTRAINT uc_movie_title UNIQUE (title);

ALTER TABLE actor
DROP
COLUMN date_of_birth;

ALTER TABLE actor
    ADD date_of_birth datetime NOT NULL;

ALTER TABLE movie
DROP
COLUMN release_date;

ALTER TABLE movie
    ADD release_date datetime NOT NULL;