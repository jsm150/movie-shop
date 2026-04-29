package com.movie.shop.api.movie.infrastructure.persistence;

import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.condition.MovieTitleUniquenessCondition;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.port.MovieTitleUniquenessConditionPort;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieJpaAdapter extends JpaRepository<Movie, Long>, MovieJpaPort, MovieTitleUniquenessConditionPort {

    boolean existsByTitle_Title(String title);

    @Override
    default MovieTitleUniquenessCondition findCondition(String title) {
        return new MovieTitleUniquenessCondition(!existsByTitle_Title(title));
    }
}
