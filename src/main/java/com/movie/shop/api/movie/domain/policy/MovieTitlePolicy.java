package com.movie.shop.api.movie.domain.policy;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;

import java.util.Objects;

public class MovieTitlePolicy {

    private final MovieJpaPort movieJpaPort;

    public MovieTitlePolicy(MovieJpaPort movieJpaPort) {
        this.movieJpaPort = Objects.requireNonNull(movieJpaPort, "영화 제목 중복 조회 포트가 필수입니다.");
    }

    public void validateNotDuplicate(String title) {
        if (movieJpaPort.loadTitleDuplication(title)) {
            throw new MovieDomainException("동일한 제목의 영화가 이미 존재합니다.");
        }
    }
}
