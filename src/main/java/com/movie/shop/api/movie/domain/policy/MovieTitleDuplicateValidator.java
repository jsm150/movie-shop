package com.movie.shop.api.movie.domain.policy;

import java.util.Objects;

import com.movie.shop.api.movie.domain.policy.status.MovieTitleDuplication;

public class MovieTitleDuplicateValidator {

    private final MovieTitleDuplication titleDuplication;

    public MovieTitleDuplicateValidator(MovieTitleDuplication titleDuplication) {
        this.titleDuplication = Objects.requireNonNull(titleDuplication, "영화 제목 중복 정보는 필수입니다.");
    }

    public boolean validateNotDuplicate(String movieTitle) {
        return !titleDuplication.duplicated();
    }
}
