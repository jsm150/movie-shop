package com.movie.shop.api.movie.domain.exceptions;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.List;

public class MovieDomainException extends DomainException {
    
    public MovieDomainException(String message) {
        super(message);
    }
    
    public MovieDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public MovieDomainException(List<String> errors) {
        super(errors);
    }
}
