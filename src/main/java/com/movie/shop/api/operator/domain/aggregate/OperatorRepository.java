package com.movie.shop.api.operator.domain.aggregate;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import com.movie.shop.api.operator.domain.port.LoadOperatorPort;
import com.movie.shop.api.operator.domain.port.OperatorJpaPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OperatorRepository implements LoadOperatorPort {

    private final OperatorJpaPort operatorJpaPort;

    public Operator save(Operator operator) {
        return operatorJpaPort.save(operator);
    }

    @Override
    public Operator getById(long operatorId) {
        return operatorJpaPort.findById(operatorId)
                .orElseThrow(() -> new OperatorDomainException("운영자 데이터가 존재하지 않습니다."));
    }

    public Operator getByLoginId(String loginId) {
        return operatorJpaPort.findByLoginId(loginId)
                .orElseThrow(() -> new BadCredentialsException("로그인 ID 또는 비밀번호가 올바르지 않습니다."));
    }

    public boolean existsByLoginId(String loginId) {
        return operatorJpaPort.existsByLoginId(loginId);
    }
}
