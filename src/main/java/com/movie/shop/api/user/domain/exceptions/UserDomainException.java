package com.movie.shop.api.user.domain.exceptions;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.List;

public class UserDomainException extends DomainException {

    public UserDomainException(String message) {
        super(message);
    }

    public UserDomainException(List<String> errors) {
        super(errors);
    }
}
