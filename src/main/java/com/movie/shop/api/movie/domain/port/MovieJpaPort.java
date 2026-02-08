package com.movie.shop.api.movie.domain.port;

import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.policy.MovieTitleDuplication;

import java.util.Optional;

public interface MovieJpaPort {

    Movie save(Movie movie);

    Optional<Movie> findById(Long movieId);

    void delete(Movie movie);

    long count();

    MovieTitleDuplication loadTitleDuplication(String title);
}
