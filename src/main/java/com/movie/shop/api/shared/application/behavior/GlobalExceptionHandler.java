package com.movie.shop.api.shared.application.behavior;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.movie.shop.api.operator.domain.exceptions.OperatorAuthorizationException;
import com.movie.shop.api.shared.domain.exceptions.DomainException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OperatorAuthorizationException.class)
    public ProblemDetail handleOperatorAuthorizationException(
            OperatorAuthorizationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );

        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        
        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.hasMultipleErrors()) {
            problemDetail.setProperty("errors", ex.getErrors());
        }
        
        return problemDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );

        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
