package com.movie.shop.api.auditorium.domain.port;

import java.util.Optional;

import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterActivationStatus;

public interface LoadAuditoriumTheaterActivationStatusPort {

    Optional<AuditoriumTheaterActivationStatus> loadAuditoriumTheaterActivationStatus(long theaterId);
}
