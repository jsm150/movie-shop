package com.movie.shop.api.movie.domain.port;

import com.movie.shop.api.movie.domain.policy.MovieScreeningLinkStatus;

public interface CheckMovieScreeningLinkPort {

    MovieScreeningLinkStatus loadMovieScreeningLinkStatus(long movieId);
}
