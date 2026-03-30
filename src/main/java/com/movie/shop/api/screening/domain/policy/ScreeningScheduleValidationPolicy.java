package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.port.LoadAuditoriumScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;

import java.util.Objects;

public record ScreeningScheduleValidationPolicy(LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort,
                                                LoadAuditoriumScreeningAvailabilityPort loadAuditoriumScreeningAvailabilityPort) {

    public ScreeningScheduleValidationPolicy {
        Objects.requireNonNull(loadMovieSchedulingAvailabilityPort, "영화 상영 가능 정보 조회 포트는 필수입니다.");
        Objects.requireNonNull(loadAuditoriumScreeningAvailabilityPort, "상영관 상영 가능 정보 조회 포트는 필수입니다.");
    }

    public void validate(long movieId, long auditoriumId) {
        validateMovie(movieId);
        validateAuditorium(auditoriumId);
    }

    private void validateMovie(long movieId) {
        MovieSchedulingAvailability movie = loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)
                .orElseThrow(() -> new ScreeningDomainException("영화 정보를 찾을 수 없습니다."));

        if (!movie.schedulable()) {
            throw new ScreeningDomainException("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
        }
    }

    private void validateAuditorium(long auditoriumId) {
        boolean auditoriumAvailable = loadAuditoriumScreeningAvailabilityPort
                .loadAuditoriumScreeningAvailability(auditoriumId)
                .orElseThrow(() -> new ScreeningDomainException("상영관 정보를 찾을 수 없습니다."));

        if (!auditoriumAvailable) {
            throw new ScreeningDomainException("활성화된 상영관에서만 상영 등록/수정이 가능합니다.");
        }
    }
}
