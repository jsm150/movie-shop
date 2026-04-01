package com.movie.shop.api.operator.domain.exceptions;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.List;

public class OperatorDomainException extends DomainException {

    public OperatorDomainException(String message) {
        super(message);
    }

    public OperatorDomainException(List<String> errors) {
        super(errors);
    }
}
