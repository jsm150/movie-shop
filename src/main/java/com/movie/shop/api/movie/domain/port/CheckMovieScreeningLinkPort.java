package com.movie.shop.api.movie.domain.port;

public interface CheckMovieScreeningLinkPort {

    boolean existsByMovieId(long movieId);
}
