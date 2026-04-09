package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;

public enum MovieStatus {
    PREPARING,
    COMING_SOON,
    NOW_SHOWING,
    ENDED;

    public MovieStatus transition(MovieStateChange stateChange) {
        if (stateChange == null) {
            throw new MovieDomainException("변경할 영화 상태는 필수입니다.");
        }

        if (this == PREPARING && stateChange == MovieStateChange.COMING_SOON) {
            return COMING_SOON;
        }
        if (this == COMING_SOON && stateChange == MovieStateChange.NOW_SHOWING) {
            return NOW_SHOWING;
        }
        if (this == NOW_SHOWING && stateChange == MovieStateChange.ENDED) {
            return ENDED;
        }

        throw invalidTransition(stateChange);
    }

    private MovieDomainException invalidTransition(MovieStateChange stateChange) {
        return switch (stateChange) {
            case COMING_SOON ->
                    new MovieDomainException("PREPARING 이 아닌 상태에서 COMING_SOON으로 변경하려고 함.");
            case NOW_SHOWING ->
                    new MovieDomainException("COMING_SOON 이 아닌 상태에서 NOW_SHOWING으로 변경하려고 함.");
            case ENDED ->
                    new MovieDomainException("NOW_SHOWING 이 아닌 상태에서 ENDED로 변경하려고 함.");
        };
    }
}
