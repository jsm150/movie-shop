package com.movie.shop.api.movie.infrastructure.policy;

import com.movie.shop.api.movie.domain.condition.MovieScreeningPresence;
import com.movie.shop.api.movie.domain.port.MovieScreeningPresencePort;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieScreeningPresenceAdapter implements MovieScreeningPresencePort {

    private final ScreeningJpaPort screeningJpaPort;

    @Override
    public MovieScreeningPresence findPresence(long movieId) {
        return new MovieScreeningPresence(screeningJpaPort.existsByMovieId(movieId));
    }
}
