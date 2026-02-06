package com.movie.shop.api.shared.domain;

import io.vavr.control.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilsTest {

    private static final String ERROR_MESSAGE = "error";

    @Test
    @DisplayName("null 검증에서 null이 아닌 값을 전달하면 유효 결과를 반환한다")
    void notNull_withNonNullValue_returnsValid() {
        Validation<String, String> result = ValidationUtils.notNull("value", ERROR_MESSAGE);

        assertValid(result, "value");
    }

    @Test
    @DisplayName("null 검증에서 null 값을 전달하면 오류 결과를 반환한다")
    void notNull_withNullValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notNull(null, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("공백 검증에서 공백이 아닌 문자열을 전달하면 유효 결과를 반환한다")
    void notBlank_withNonBlankValue_returnsValid() {
        Validation<String, String> result = ValidationUtils.notBlank("  value  ", ERROR_MESSAGE);

        assertValid(result, "  value  ");
    }

    @Test
    @DisplayName("공백 검증에서 공백 문자열을 전달하면 오류 결과를 반환한다")
    void notBlank_withBlankValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notBlank("   ", ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("공백 검증에서 null 값을 전달하면 오류 결과를 반환한다")
    void notBlank_withNullValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notBlank(null, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("빈 문자열 검증에서 빈 문자열이 아닌 값을 전달하면 유효 결과를 반환한다")
    void notEmpty_withNonEmptyString_returnsValid() {
        Validation<String, String> result = ValidationUtils.notEmpty("value", ERROR_MESSAGE);

        assertValid(result, "value");
    }

    @Test
    @DisplayName("빈 문자열 검증에서 빈 문자열을 전달하면 오류 결과를 반환한다")
    void notEmpty_withEmptyString_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notEmpty("", ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("빈 컬렉션 검증에서 비어 있지 않은 컬렉션을 전달하면 유효 결과를 반환한다")
    void notEmptyCollection_withNonEmptyCollection_returnsValid() {
        List<Integer> collection = List.of(1, 2, 3);

        Validation<String, List<Integer>> result = ValidationUtils.notEmptyCollection(collection, ERROR_MESSAGE);

        assertValid(result, collection);
    }

    @Test
    @DisplayName("빈 컬렉션 검증에서 비어 있는 컬렉션을 전달하면 오류 결과를 반환한다")
    void notEmptyCollection_withEmptyCollection_returnsInvalid() {
        Validation<String, List<Integer>> result = ValidationUtils.notEmptyCollection(List.of(), ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("문자열 길이 범위 검증에서 범위 내 값을 전달하면 유효 결과를 반환한다")
    void size_withValueWithinRange_returnsValid() {
        Validation<String, String> result = ValidationUtils.size("abcd", 2, 4, ERROR_MESSAGE);

        assertValid(result, "abcd");
    }

    @Test
    @DisplayName("문자열 길이 범위 검증에서 범위 밖 값을 전달하면 오류 결과를 반환한다")
    void size_withValueOutsideRange_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.size("a", 2, 4, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("최소 길이 검증에서 최솟값 이상 값을 전달하면 유효 결과를 반환한다")
    void minLength_withValueMeetingMinimum_returnsValid() {
        Validation<String, String> result = ValidationUtils.minLength("abc", 3, ERROR_MESSAGE);

        assertValid(result, "abc");
    }

    @Test
    @DisplayName("최소 길이 검증에서 최솟값 미만 값을 전달하면 오류 결과를 반환한다")
    void minLength_withValueBelowMinimum_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.minLength("ab", 3, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("최대 길이 검증에서 최대 길이 이하 값을 전달하면 유효 결과를 반환한다")
    void maxLength_withValueWithinLimit_returnsValid() {
        Validation<String, String> result = ValidationUtils.maxLength("abc", 3, ERROR_MESSAGE);

        assertValid(result, "abc");
    }

    @Test
    @DisplayName("최대 길이 검증에서 최대 길이를 초과한 값을 전달하면 오류 결과를 반환한다")
    void maxLength_withValueExceedingLimit_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.maxLength("abcd", 3, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("컬렉션 크기 범위 검증에서 범위 내 크기의 컬렉션을 전달하면 유효 결과를 반환한다")
    void sizeCollection_withCollectionWithinRange_returnsValid() {
        List<Integer> collection = List.of(1, 2, 3);

        Validation<String, List<Integer>> result = ValidationUtils.sizeCollection(collection, 2, 3, ERROR_MESSAGE);

        assertValid(result, collection);
    }

    @Test
    @DisplayName("컬렉션 크기 범위 검증에서 범위 밖 크기의 컬렉션을 전달하면 오류 결과를 반환한다")
    void sizeCollection_withCollectionOutsideRange_returnsInvalid() {
        Validation<String, List<Integer>> result = ValidationUtils.sizeCollection(List.of(1), 2, 3, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("최댓값 검증에서 상한값과 같은 값을 전달하면 유효 결과를 반환한다")
    void max_withValueAtUpperBound_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.max(10, 10, ERROR_MESSAGE);

        assertValid(result, 10);
    }

    @Test
    @DisplayName("최댓값 검증에서 상한값을 초과한 값을 전달하면 오류 결과를 반환한다")
    void max_withValueAboveUpperBound_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.max(11, 10, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("최솟값 검증에서 하한값과 같은 값을 전달하면 유효 결과를 반환한다")
    void min_withValueAtLowerBound_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.min(10, 10, ERROR_MESSAGE);

        assertValid(result, 10);
    }

    @Test
    @DisplayName("최솟값 검증에서 하한값보다 작은 값을 전달하면 오류 결과를 반환한다")
    void min_withValueBelowLowerBound_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.min(9, 10, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("범위 검증에서 경계 범위 내 값을 전달하면 유효 결과를 반환한다")
    void range_withValueWithinBounds_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.range(5, 1, 10, ERROR_MESSAGE);

        assertValid(result, 5);
    }

    @Test
    @DisplayName("범위 검증에서 경계 범위 밖 값을 전달하면 오류 결과를 반환한다")
    void range_withValueOutsideBounds_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.range(0, 1, 10, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("양수 검증에서 양의 정수를 전달하면 유효 결과를 반환한다")
    void positive_integerWithPositiveValue_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.positive(1, ERROR_MESSAGE);

        assertValid(result, 1);
    }

    @Test
    @DisplayName("양수 검증에서 정수 0을 전달하면 오류 결과를 반환한다")
    void positive_integerWithZero_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.positive(0, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("양수 검증에서 양의 long 값을 전달하면 유효 결과를 반환한다")
    void positive_longWithPositiveValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.positive(1L, ERROR_MESSAGE);

        assertValid(result, 1L);
    }

    @Test
    @DisplayName("양수 검증에서 음의 long 값을 전달하면 오류 결과를 반환한다")
    void positive_longWithNegativeValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.positive(-1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("양수 검증에서 양의 double 값을 전달하면 유효 결과를 반환한다")
    void positive_doubleWithPositiveValue_returnsValid() {
        Validation<String, Double> result = ValidationUtils.positive(0.1d, ERROR_MESSAGE);

        assertValid(result, 0.1d);
    }

    @Test
    @DisplayName("양수 검증에서 double 0.0 값을 전달하면 오류 결과를 반환한다")
    void positive_doubleWithZero_returnsInvalid() {
        Validation<String, Double> result = ValidationUtils.positive(0.0d, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("0 이상 검증에서 정수 0을 전달하면 유효 결과를 반환한다")
    void positiveOrZero_integerWithZero_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.positiveOrZero(0, ERROR_MESSAGE);

        assertValid(result, 0);
    }

    @Test
    @DisplayName("0 이상 검증에서 음의 정수를 전달하면 오류 결과를 반환한다")
    void positiveOrZero_integerWithNegativeValue_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.positiveOrZero(-1, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("0 이상 검증에서 양의 long 값을 전달하면 유효 결과를 반환한다")
    void positiveOrZero_longWithPositiveValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.positiveOrZero(1L, ERROR_MESSAGE);

        assertValid(result, 1L);
    }

    @Test
    @DisplayName("0 이상 검증에서 음의 long 값을 전달하면 오류 결과를 반환한다")
    void positiveOrZero_longWithNegativeValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.positiveOrZero(-1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("음수 검증에서 음의 정수를 전달하면 유효 결과를 반환한다")
    void negative_integerWithNegativeValue_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.negative(-1, ERROR_MESSAGE);

        assertValid(result, -1);
    }

    @Test
    @DisplayName("음수 검증에서 정수 0을 전달하면 오류 결과를 반환한다")
    void negative_integerWithZero_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.negative(0, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("음수 검증에서 음의 long 값을 전달하면 유효 결과를 반환한다")
    void negative_longWithNegativeValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.negative(-1L, ERROR_MESSAGE);

        assertValid(result, -1L);
    }

    @Test
    @DisplayName("음수 검증에서 양의 long 값을 전달하면 오류 결과를 반환한다")
    void negative_longWithPositiveValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.negative(1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("0 이하 검증에서 정수 0을 전달하면 유효 결과를 반환한다")
    void negativeOrZero_integerWithZero_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.negativeOrZero(0, ERROR_MESSAGE);

        assertValid(result, 0);
    }

    @Test
    @DisplayName("0 이하 검증에서 양의 정수를 전달하면 오류 결과를 반환한다")
    void negativeOrZero_integerWithPositiveValue_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.negativeOrZero(1, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("0 이하 검증에서 음의 long 값을 전달하면 유효 결과를 반환한다")
    void negativeOrZero_longWithNegativeValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.negativeOrZero(-1L, ERROR_MESSAGE);

        assertValid(result, -1L);
    }

    @Test
    @DisplayName("0 이하 검증에서 양의 long 값을 전달하면 오류 결과를 반환한다")
    void negativeOrZero_longWithPositiveValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.negativeOrZero(1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("패턴 검증에서 패턴과 일치하는 값을 전달하면 유효 결과를 반환한다")
    void pattern_withMatchingValue_returnsValid() {
        Validation<String, String> result = ValidationUtils.pattern("ABC123", "^[A-Z0-9]+$", ERROR_MESSAGE);

        assertValid(result, "ABC123");
    }

    @Test
    @DisplayName("패턴 검증에서 패턴과 일치하지 않는 값을 전달하면 오류 결과를 반환한다")
    void pattern_withNonMatchingValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.pattern("abc123", "^[A-Z0-9]+$", ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    @DisplayName("이메일 형식 검증에서 유효한 이메일을 전달하면 유효 결과를 반환한다")
    void email_withValidEmail_returnsValid() {
        Validation<String, String> result = ValidationUtils.email("test.user+shop@example.com", ERROR_MESSAGE);

        assertValid(result, "test.user+shop@example.com");
    }

    @Test
    @DisplayName("이메일 형식 검증에서 유효하지 않은 이메일을 전달하면 오류 결과를 반환한다")
    void email_withInvalidEmail_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.email("invalid-email", ERROR_MESSAGE);

        assertInvalid(result);
    }

    private <T> void assertValid(Validation<String, T> result, T expectedValue) {
        assertThat(result.isValid()).isTrue();
        assertThat(result.get()).isEqualTo(expectedValue);
    }

    private <T> void assertInvalid(Validation<String, T> result) {
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getError()).isEqualTo(ERROR_MESSAGE);
    }
}
