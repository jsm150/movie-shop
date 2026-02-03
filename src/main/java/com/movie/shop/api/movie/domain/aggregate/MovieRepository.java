package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle_Title(String title);

    default Movie getById(long movieId) {
        return this.findById(movieId)
                .orElseThrow(() -> new MovieDomainException("영화 데이터가 존재하지 않습니다."));
    }
}
