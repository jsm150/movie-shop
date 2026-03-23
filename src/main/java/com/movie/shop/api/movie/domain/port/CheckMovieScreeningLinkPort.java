package com.movie.shop.api.movie.domain.port;

import com.movie.shop.api.movie.domain.policy.status.MovieScreeningLinkStatus;

public interface CheckMovieScreeningLinkPort {

    MovieScreeningLinkStatus loadMovieScreeningLinkStatus(long movieId);
}
