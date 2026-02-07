package com.movie.shop.api.theater.domain.port;

public interface CheckTheaterScreeningLinkPort {

    boolean existsBlockingScreeningByTheaterId(long theaterId);
}
