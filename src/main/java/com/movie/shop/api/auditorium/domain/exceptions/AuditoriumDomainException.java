package com.movie.shop.api.auditorium.domain.exceptions;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.List;

public class AuditoriumDomainException extends DomainException {

    public AuditoriumDomainException(String message) {
        super(message);
    }

    public AuditoriumDomainException(List<String> errors) {
        super(errors);
    }
}
