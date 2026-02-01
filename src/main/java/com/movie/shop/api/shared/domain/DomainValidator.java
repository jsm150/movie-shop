package com.movie.shop.api.shared.domain;

import com.movie.shop.api.shared.domain.exceptions.DomainException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 도메인 엔티티 생성 시 여러 필드의 유효성 검증을 수행하고,
 * 모든 검증 실패 케이스를 모아서 한번에 DomainException으로 던지는 유틸리티 클래스.
 * 사용 예시:
 * <pre>
 * DomainValidator.builder()
 *     .validate(() -> title != null && !title.isBlank(), "제목은 필수입니다.")
 *     .validate(() -> price >= 0, "가격은 0 이상이어야 합니다.")
 *     .validate(() -> releaseDate != null, "개봉일은 필수입니다.")
 *     .throwIfInvalid();
 * </pre>
 */
public class DomainValidator {

    private final List<String> errors;

    private DomainValidator() {
        this.errors = new ArrayList<>();
    }

    public static DomainValidator builder() {
        return new DomainValidator();
    }

    /**
     * 조건이 false일 경우 에러 메시지를 추가합니다.
     * 
     * @param condition 검증 조건 (true면 통과, false면 실패)
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator validate(boolean condition, String errorMessage) {
        if (!condition) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 조건이 false일 경우 에러 메시지를 추가합니다. (지연 평가)
     * 
     * @param conditionSupplier 검증 조건 공급자 (true면 통과, false면 실패)
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator validate(Supplier<Boolean> conditionSupplier, String errorMessage) {
        try {
            if (!conditionSupplier.get()) {
                errors.add(errorMessage);
            }
        } catch (Exception e) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 값이 null이 아닌지 검증합니다.
     * 
     * @param value 검증할 값
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator notNull(Object value, String errorMessage) {
        if (value == null) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 문자열이 null이 아니고 비어있지 않은지 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator notBlank(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 숫자가 양수인지 검증합니다.
     * 
     * @param value 검증할 숫자
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator positive(Number value, String errorMessage) {
        if (value == null || value.doubleValue() <= 0) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 숫자가 0 이상인지 검증합니다.
     * 
     * @param value 검증할 숫자
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator notNegative(Number value, String errorMessage) {
        if (value == null || value.doubleValue() < 0) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 문자열 길이가 최대값을 초과하지 않는지 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param maxLength 최대 길이
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator maxLength(String value, int maxLength, String errorMessage) {
        if (value != null && value.length() > maxLength) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 문자열 길이가 최소값 이상인지 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param minLength 최소 길이
     * @param errorMessage 실패 시 에러 메시지
     * @return this
     */
    public DomainValidator minLength(String value, int minLength, String errorMessage) {
        if (value == null || value.length() < minLength) {
            errors.add(errorMessage);
        }
        return this;
    }

    /**
     * 현재까지 검증 실패가 있는지 확인합니다.
     * 
     * @return 검증 실패가 있으면 true
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 현재까지의 에러 목록을 반환합니다.
     * 
     * @return 에러 메시지 목록
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * 검증 실패가 있으면 커스텀 예외를 던집니다.
     * 
     * @param exceptionSupplier 예외 생성자 (에러 목록을 받아 예외를 생성)
     * @param <T> 예외 타입
     * @throws T 검증 실패가 하나 이상 있을 경우
     */
    public <T extends DomainException> void throwIfInvalid(
            java.util.function.Function<List<String>, T> exceptionSupplier) {
        if (hasErrors()) {
            throw exceptionSupplier.apply(errors);
        }
    }
}
