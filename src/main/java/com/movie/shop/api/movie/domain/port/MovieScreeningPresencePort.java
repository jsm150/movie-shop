package com.movie.shop.api.movie.domain.port;

import com.movie.shop.api.movie.domain.condition.MovieScreeningPresence;

public interface MovieScreeningPresencePort {

    MovieScreeningPresence findPresence(long movieId);
}
