package com.movie.shop.api.operator.domain.port;

@FunctionalInterface
public interface CheckOperatorTheaterExistencePort {

    boolean existsTheater(long theaterId);
}
