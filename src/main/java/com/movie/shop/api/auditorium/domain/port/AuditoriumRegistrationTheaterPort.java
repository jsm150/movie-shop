package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumRegistrationTheater;

import java.util.Optional;

public interface AuditoriumRegistrationTheaterPort {

    Optional<AuditoriumRegistrationTheater> findRegistrationTheater(long theaterId);
}
