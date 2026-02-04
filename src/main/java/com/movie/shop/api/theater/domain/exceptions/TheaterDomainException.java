package com.movie.shop.api.theater.domain.exceptions;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.List;

public class TheaterDomainException extends DomainException {

    public TheaterDomainException(String message) {
        super(message);
    }

    public TheaterDomainException(List<String> errors) {
        super(errors);
    }
}
