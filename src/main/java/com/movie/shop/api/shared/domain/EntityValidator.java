package com.movie.shop.api.shared.domain;

import io.vavr.collection.Seq;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Jakarta Validation과 수동 검증을 통합하여 처리하는 유틸리티 클래스.
 * 어노테이션 기반 검증과 비즈니스 로직 검증(예: 중복 체크)을 하나의 예외로 통합합니다.
 */
public class EntityValidator {
    
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    private final List<String> errors = new ArrayList<>();
    
    private EntityValidator() {}
    
    public static EntityValidator create() {
        return new EntityValidator();
    }
    
    /**
     * Jakarta Validation 어노테이션 기반 검증을 수행합니다.
     */
    public <T> EntityValidator validateBean(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getMessage());
        }
        return this;
    }

    /**
     * 조건이 false일 경우 에러 메시지를 추가합니다.
     *
     * @param condition 검증 조건 (true면 통과, false면 실패)
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public EntityValidator validate(boolean condition, String errorMessage) {
        if (!condition) {
            errors.add(errorMessage);
        }
        return this;
    }
    
    public <T> EntityValidator apply(io.vavr.control.Validation<Seq<String>, T> result, Consumer<T> setter) {
        if (!result.isValid()) {
            result.getError().forEach(errors::add);
        }
        else {
            setter.accept(result.get());
        }
        return this;
    }
    
    /**
     * 수집된 에러가 있는지 확인합니다.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * 수집된 에러 목록을 반환합니다.
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 수집된 에러가 있으면 제공된 예외 팩토리로 예외를 던집니다.
     */
    public <E extends RuntimeException> void throwIfInvalid(Function<List<String>, E> exceptionFactory) {
        if (hasErrors()) {
            throw exceptionFactory.apply(getErrors());
        }
    }
}
