package com.movie.shop.api.movie.domain.aggregate.validator;

import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieTitleDuplicateValidator {
    private final MovieRepository movieRepository;

    public boolean validateNotDuplicate(String movieTitle) {
        return !movieRepository.existsByTitle_Title(movieTitle);
    }
}
