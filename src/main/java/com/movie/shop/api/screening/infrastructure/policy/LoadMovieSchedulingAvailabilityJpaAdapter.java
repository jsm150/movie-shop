package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.MovieSchedulingAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadMovieSchedulingAvailabilityJpaAdapter implements LoadMovieSchedulingAvailabilityPort {

    private final MovieJpaPort movieJpaPort;

    @Override
    public Optional<MovieSchedulingAvailability> loadMovieSchedulingAvailability(long movieId) {
        return movieJpaPort.findById(movieId)
                .map(movie -> new MovieSchedulingAvailability(movie.canBeScheduled(), movie.getRuntimeMinutes()));
    }
}
