package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

import java.util.Optional;

public record ScreeningScheduleValidationPolicy(Optional<MovieSchedulingAvailability> movieSchedulingAvailability,
                                                Optional<TheaterScreeningAvailability> theaterScreeningAvailability) {

    public void validate() {
        validateMovie();
        validateTheater();
    }

    private void validateMovie() {
        MovieSchedulingAvailability movie = movieSchedulingAvailability
                .orElseThrow(() -> new ScreeningDomainException("영화 정보를 찾을 수 없습니다."));

        if (!movie.schedulable()) {
            throw new ScreeningDomainException("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
        }
    }

    private void validateTheater() {
        TheaterScreeningAvailability theater = theaterScreeningAvailability
                .orElseThrow(() -> new ScreeningDomainException("극장 정보를 찾을 수 없습니다."));

        if (!theater.available()) {
            throw new ScreeningDomainException("활성화된 극장에서만 상영 등록/수정이 가능합니다.");
        }
    }
}
