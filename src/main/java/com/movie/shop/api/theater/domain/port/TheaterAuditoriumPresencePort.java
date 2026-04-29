package com.movie.shop.api.theater.domain.port;

import com.movie.shop.api.theater.domain.condition.TheaterAuditoriumPresence;

public interface TheaterAuditoriumPresencePort {

    TheaterAuditoriumPresence findPresence(long theaterId);
}
