package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumOperatingTheaterStatus;

import java.util.Optional;

public interface AuditoriumOperatingTheaterStatusPort {

    Optional<AuditoriumOperatingTheaterStatus> findStatus(long theaterId);
}
