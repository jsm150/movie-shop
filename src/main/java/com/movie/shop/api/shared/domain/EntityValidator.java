package com.movie.shop.api.shared.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Jakarta Validation 어노테이션 기반 검증 결과를 도메인 예외로 변환합니다.
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
     * 수집된 에러가 있는지 확인합니다.
     */
    private boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * 수집된 에러 목록을 반환합니다.
     */
    private List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 수집된 에러가 있으면 제공된 예외 팩토리로 예외를 던집니다.
     */
    public <E extends RuntimeException> void throwIfInvalid(ExceptionFactory<E> exceptionFactory) {
        if (hasErrors()) {
            throw exceptionFactory.create(getErrors());
        }
    }

    @FunctionalInterface
    public interface ExceptionFactory<E extends RuntimeException> {
        E create(List<String> errors);
    }
}
