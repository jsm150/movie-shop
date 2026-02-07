package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TheaterNameDuplicateValidator {
    private final TheaterRepository theaterRepository;

    public boolean validateNotDuplicate(String theaterName) {
        return !theaterRepository.existsByName(theaterName);
    }
}