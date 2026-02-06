package com.movie.shop.api.shared.domain;

import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.Collection;

public class ValidationUtils {

    // ==================== Null 검증 ====================

    public static <T> Validation<String, T> notNull(T obj, String errorMessage) {
        return Option.of(obj)
                .toValidation(errorMessage);
    }

    // ==================== 문자열 검증 ====================

    /**
     * 문자열이 null이 아니고, 빈 문자열이 아니며, 공백만으로 이루어지지 않았는지 검증
     * @NotBlank 와 동일한 기능
     */
    public static Validation<String, String> notBlank(String str, String errorMessage) {
        return Option.of(str)
                .filter(s -> !s.trim().isEmpty())
                .toValidation(errorMessage);
    }

    /**
     * 문자열이 null이 아니고, 빈 문자열이 아닌지 검증
     * @NotEmpty 와 동일한 기능 (문자열용)
     */
    public static Validation<String, String> notEmpty(String str, String errorMessage) {
        return Option.of(str)
                .filter(s -> !s.isEmpty())
                .toValidation(errorMessage);
    }

    // ==================== 컬렉션 검증 ====================

    /**
     * 컬렉션이 null이 아니고, 비어있지 않은지 검증
     * @NotEmpty 와 동일한 기능 (컬렉션용)
     */
    public static <T extends Collection<?>> Validation<String, T> notEmptyCollection(T collection, String errorMessage) {
        return Option.of(collection)
                .filter(c -> !c.isEmpty())
                .toValidation(errorMessage);
    }

    // ==================== 크기 검증 ====================

    /**
     * 문자열의 길이가 지정된 범위 내에 있는지 검증
     * @Size 와 동일한 기능 (문자열용)
     */
    public static Validation<String, String> size(String str, int min, int max, String errorMessage) {
        return Option.of(str)
                .filter(s -> s.length() >= min && s.length() <= max)
                .toValidation(errorMessage);
    }

    /**
     * 문자열의 최소 길이를 검증
     */
    public static Validation<String, String> minLength(String str, int min, String errorMessage) {
        return Option.of(str)
                .filter(s -> s.length() >= min)
                .toValidation(errorMessage);
    }

    /**
     * 문자열의 최대 길이를 검증
     */
    public static Validation<String, String> maxLength(String str, int max, String errorMessage) {
        return Option.of(str)
                .filter(s -> s.length() <= max)
                .toValidation(errorMessage);
    }

    /**
     * 컬렉션의 크기가 지정된 범위 내에 있는지 검증
     * @Size 와 동일한 기능 (컬렉션용)
     */
    public static <T extends Collection<?>> Validation<String, T> sizeCollection(T collection, int min, int max, String errorMessage) {
        return Option.of(collection)
                .filter(c -> c.size() >= min && c.size() <= max)
                .toValidation(errorMessage);
    }

    // ==================== 숫자 범위 검증 ====================

    /**
     * 숫자가 최대값 이하인지 검증
     * @Max 와 동일한 기능
     */
    public static <T extends Number & Comparable<T>> Validation<String, T> max(T value, T maxValue, String errorMessage) {
        return Option.of(value)
                .filter(v -> v.compareTo(maxValue) <= 0)
                .toValidation(errorMessage);
    }

    /**
     * 숫자가 최소값 이상인지 검증
     * @Min 와 동일한 기능
     */
    public static <T extends Number & Comparable<T>> Validation<String, T> min(T value, T minValue, String errorMessage) {
        return Option.of(value)
                .filter(v -> v.compareTo(minValue) >= 0)
                .toValidation(errorMessage);
    }

    /**
     * 숫자가 지정된 범위 내에 있는지 검증
     */
    public static <T extends Number & Comparable<T>> Validation<String, T> range(T value, T minValue, T maxValue, String errorMessage) {
        return Option.of(value)
                .filter(v -> v.compareTo(minValue) >= 0 && v.compareTo(maxValue) <= 0)
                .toValidation(errorMessage);
    }

    // ==================== 양수/음수 검증 ====================

    /**
     * 숫자가 양수인지 검증
     * @Positive 와 동일한 기능
     */
    public static Validation<String, Integer> positive(Integer value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v > 0)
                .toValidation(errorMessage);
    }

    public static Validation<String, Long> positive(Long value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v > 0)
                .toValidation(errorMessage);
    }

    public static Validation<String, Double> positive(Double value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v > 0)
                .toValidation(errorMessage);
    }

    /**
     * 숫자가 양수 또는 0인지 검증
     * @PositiveOrZero 와 동일한 기능
     */
    public static Validation<String, Integer> positiveOrZero(Integer value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v >= 0)
                .toValidation(errorMessage);
    }

    public static Validation<String, Long> positiveOrZero(Long value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v >= 0)
                .toValidation(errorMessage);
    }

    /**
     * 숫자가 음수인지 검증
     * @Negative 와 동일한 기능
     */
    public static Validation<String, Integer> negative(Integer value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v < 0)
                .toValidation(errorMessage);
    }

    public static Validation<String, Long> negative(Long value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v < 0)
                .toValidation(errorMessage);
    }

    /**
     * 숫자가 음수 또는 0인지 검증
     * @NegativeOrZero 와 동일한 기능
     */
    public static Validation<String, Integer> negativeOrZero(Integer value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v <= 0)
                .toValidation(errorMessage);
    }

    public static Validation<String, Long> negativeOrZero(Long value, String errorMessage) {
        return Option.of(value)
                .filter(v -> v <= 0)
                .toValidation(errorMessage);
    }

    // ==================== 패턴 검증 ====================

    /**
     * 문자열이 정규표현식 패턴과 일치하는지 검증
     * @Pattern 와 동일한 기능
     */
    public static Validation<String, String> pattern(String str, String regex, String errorMessage) {
        return Option.of(str)
                .filter(s -> s.matches(regex))
                .toValidation(errorMessage);
    }

    // ==================== 이메일 검증 ====================

    /**
     * 이메일 형식인지 검증
     * @Email 와 동일한 기능
     */
    public static Validation<String, String> email(String str, String errorMessage) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return pattern(str, emailRegex, errorMessage);
    }
}
