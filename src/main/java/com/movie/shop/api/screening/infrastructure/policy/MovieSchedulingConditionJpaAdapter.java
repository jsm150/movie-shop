package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.screening.domain.condition.MovieSchedulingCondition;
import com.movie.shop.api.screening.domain.port.MovieSchedulingConditionPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MovieSchedulingConditionJpaAdapter implements MovieSchedulingConditionPort {

    private final MovieJpaPort movieJpaPort;

    @Override
    public Optional<MovieSchedulingCondition> findCondition(long movieId) {
        return movieJpaPort.findById(movieId)
                .map(movie -> new MovieSchedulingCondition(movie.canBeScheduled(), movie.getRuntimeMinutes()));
    }
}
