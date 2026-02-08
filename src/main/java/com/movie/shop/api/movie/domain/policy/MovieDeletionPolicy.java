package com.movie.shop.api.movie.domain.policy;

import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import java.util.Objects;

public class MovieDeletionPolicy {

    private final MovieScreeningLinkStatus movieScreeningLinkStatus;

    public MovieDeletionPolicy(MovieScreeningLinkStatus movieScreeningLinkStatus) {
        this.movieScreeningLinkStatus = Objects.requireNonNull(movieScreeningLinkStatus, "영화 상영 연결 정보는 필수입니다.");
    }

    public void validateCanDelete(Movie movie) {
        movie.validateCanRemove();

        if (movie.getId() == null) {
            throw new MovieDomainException("영화 ID가 존재하지 않습니다.");
        }

        if (movieScreeningLinkStatus.linked()) {
            throw new MovieDomainException("상영이 연결된 영화는 삭제할 수 없습니다.");
        }
    }
}
