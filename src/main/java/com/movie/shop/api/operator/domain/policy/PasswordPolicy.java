package com.movie.shop.api.operator.domain.policy;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasswordPolicy {

    private final PasswordEncoder passwordEncoder;

    public void validate(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new BadCredentialsException("로그인 ID 또는 비밀번호가 올바르지 않습니다.");
        }
    }
}