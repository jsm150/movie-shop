package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterExistenceStatus;

public interface LoadAuditoriumTheaterExistenceStatusPort {

    AuditoriumTheaterExistenceStatus loadAuditoriumTheaterExistenceStatus(long theaterId);
}
