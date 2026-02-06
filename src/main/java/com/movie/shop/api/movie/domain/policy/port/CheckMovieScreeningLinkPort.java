package com.movie.shop.api.movie.domain.policy.port;

public interface CheckMovieScreeningLinkPort {

    boolean existsByMovieId(long movieId);
}
