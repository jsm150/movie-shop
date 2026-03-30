package com.movie.shop.api.movie.infrastructure.persistence;

import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieJpaAdapter extends JpaRepository<Movie, Long>, MovieJpaPort {

    boolean existsByTitle_Title(String title);

    @Override
    default boolean loadTitleDuplication(String title) {
        return existsByTitle_Title(title);
    }
}
