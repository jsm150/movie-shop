package com.movie.shop.api.theater.domain.port;

import com.movie.shop.api.theater.domain.policy.status.TheaterAuditoriumLinkStatus;

public interface CheckTheaterAuditoriumLinkPort {

    TheaterAuditoriumLinkStatus loadTheaterAuditoriumLinkStatus(long theaterId);
}
