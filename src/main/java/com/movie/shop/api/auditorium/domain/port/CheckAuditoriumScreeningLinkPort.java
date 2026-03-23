package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumScreeningLinkStatus;

public interface CheckAuditoriumScreeningLinkPort {

    AuditoriumScreeningLinkStatus loadAuditoriumScreeningLinkStatus(long auditoriumId);
}
