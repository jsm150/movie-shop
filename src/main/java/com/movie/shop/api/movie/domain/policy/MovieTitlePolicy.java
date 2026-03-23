package com.movie.shop.api.movie.domain.policy;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import java.util.Objects;

import com.movie.shop.api.movie.domain.policy.status.MovieTitleDuplication;

public class MovieTitlePolicy {

    private final MovieTitleDuplication titleDuplication;

    public MovieTitlePolicy(MovieTitleDuplication titleDuplication) {
        this.titleDuplication = Objects.requireNonNull(titleDuplication, "영화 제목 중복 정보는 필수입니다.");
    }

    public void validateNotDuplicate() {
        if (titleDuplication.duplicated()) {
            throw new MovieDomainException("동일한 제목의 영화가 이미 존재합니다.");
        }
    }
}
