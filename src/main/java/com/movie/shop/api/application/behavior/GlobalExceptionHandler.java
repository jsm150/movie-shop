package com.movie.shop.api.application.behavior;

import com.movie.shop.api.shared.domain.exceptions.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        
        problemDetail.setTitle("Domain Validation Error");
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.hasMultipleErrors()) {
            problemDetail.setProperty("errors", ex.getErrors());
        }
        
        return problemDetail;
    }
}
