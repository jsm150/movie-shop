package com.movie.shop.api.movie.domain.policy;

import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieTitleDuplicateValidator {
    private final MovieJpaPort movieJpaPort;

    public boolean validateNotDuplicate(String movieTitle) {
        return !movieJpaPort.existsByTitle(movieTitle);
    }
}