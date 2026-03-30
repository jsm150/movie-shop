package com.movie.shop.api.auditorium.domain.port;

public interface CheckAuditoriumScreeningLinkPort {

    boolean loadAuditoriumScreeningLinkStatus(long auditoriumId);
}
