package com.movie.shop.api.auditorium.domain.port;

import java.util.Optional;

public interface LoadAuditoriumTheaterActivationStatusPort {

    Optional<Boolean> loadAuditoriumTheaterActivationStatus(long theaterId);
}
