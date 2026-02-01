package com.movie.shop.api.shared.domain.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainException extends RuntimeException {
    
    private final List<String> errors;

    public DomainException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errors = List.of(message);
    }

    public DomainException(List<String> errors) {
        super(formatErrors(errors));
        this.errors = new ArrayList<>(errors);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasMultipleErrors() {
        return errors.size() > 1;
    }

    private static String formatErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation failed";
        }
        if (errors.size() == 1) {
            return errors.getFirst();
        }
        return String.format("Validation failed with %d errors: [%s]", 
                errors.size(), 
                String.join(", ", errors));
    }
}
