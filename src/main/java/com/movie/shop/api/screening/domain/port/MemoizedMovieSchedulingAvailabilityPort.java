package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MemoizedMovieSchedulingAvailabilityPort implements LoadMovieSchedulingAvailabilityPort {

    private final LoadMovieSchedulingAvailabilityPort delegate;
    private final Map<Long, Optional<MovieSchedulingAvailability>> cache = new HashMap<>();

    public MemoizedMovieSchedulingAvailabilityPort(LoadMovieSchedulingAvailabilityPort delegate) {
        this.delegate = Objects.requireNonNull(delegate, "영화 상영 가능 정보 조회 포트는 필수입니다.");
    }

    @Override
    public Optional<MovieSchedulingAvailability> loadMovieSchedulingAvailability(long movieId) {
        return cache.computeIfAbsent(movieId, delegate::loadMovieSchedulingAvailability);
    }
}
