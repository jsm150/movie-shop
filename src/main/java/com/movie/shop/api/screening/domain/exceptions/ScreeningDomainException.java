package com.movie.shop.api.screening.domain.exceptions;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.List;

public class ScreeningDomainException extends DomainException {
    public ScreeningDomainException(String message) {
        super(message);
    }

    public ScreeningDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScreeningDomainException(List<String> errors) {
        super(errors);
    }
}
