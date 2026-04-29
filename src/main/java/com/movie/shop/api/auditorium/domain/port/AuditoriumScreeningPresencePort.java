package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumScreeningPresence;

public interface AuditoriumScreeningPresencePort {

    AuditoriumScreeningPresence findPresence(long auditoriumId);
}
