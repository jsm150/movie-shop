package com.movie.shop.api.theater.domain.policy.port;

public interface CheckTheaterScreeningLinkPort {

    boolean existsBlockingScreeningByTheaterId(long theaterId);
}
