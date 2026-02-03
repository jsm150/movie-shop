package com.movie.shop.api.shared.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    
    public <T> EntityValidator apply(io.vavr.control.Validation<String, T> result, Consumer<T> setter) {
        if (!result.isValid()) {
            errors.add(result.getError());
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
