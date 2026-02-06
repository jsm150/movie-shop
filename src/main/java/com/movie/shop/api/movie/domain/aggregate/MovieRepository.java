package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.aggregate.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieRepository {

    private final MovieJpaPort movieJpaPort;

    public Movie save(Movie movie) {
        return movieJpaPort.save(movie);
    }

    public void delete(Movie movie) {
        movieJpaPort.delete(movie);
    }

    public long count() {
        return movieJpaPort.count();
    }

    public boolean existsByTitle(String title) {
        return movieJpaPort.existsByTitle(title);
    }

    public Movie getById(long movieId) {
        return movieJpaPort.findById(movieId)
                .orElseThrow(() -> new MovieDomainException("영화 데이터가 존재하지 않습니다."));
    }
}
