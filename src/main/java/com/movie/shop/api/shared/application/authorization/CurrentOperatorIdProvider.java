package com.movie.shop.api.shared.application.authorization;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentOperatorIdProvider {

    public long getCurrentOperatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }

        String subject = resolveSubject(authentication);
        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException("운영자 식별자가 올바르지 않습니다.");
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("운영자 식별자가 올바르지 않습니다.", ex);
        }
    }

    private String resolveSubject(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        return authentication.getName();
    }
}
