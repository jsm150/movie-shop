package com.movie.shop.api.theater.domain.port;

public interface CheckTheaterAuditoriumLinkPort {

    boolean loadTheaterAuditoriumLinkStatus(long theaterId);
}
