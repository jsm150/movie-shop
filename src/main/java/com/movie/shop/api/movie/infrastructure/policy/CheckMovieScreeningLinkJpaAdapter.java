package com.movie.shop.api.movie.infrastructure.policy;

import com.movie.shop.api.movie.domain.port.CheckMovieScreeningLinkPort;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckMovieScreeningLinkJpaAdapter implements CheckMovieScreeningLinkPort {

    private final ScreeningJpaPort screeningJpaPort;

    @Override
    public boolean existsByMovieId(long movieId) {
        return screeningJpaPort.existsByMovieId(movieId);
    }
}
