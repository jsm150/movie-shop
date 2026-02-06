package com.movie.shop.api.shared.domain;

import io.vavr.control.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilsTest {

    private static final String ERROR_MESSAGE = "error";

    @Test
    void notNull_withNonNullValue_returnsValid() {
        Validation<String, String> result = ValidationUtils.notNull("value", ERROR_MESSAGE);

        assertValid(result, "value");
    }

    @Test
    void notNull_withNullValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notNull(null, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void notBlank_withNonBlankValue_returnsValid() {
        Validation<String, String> result = ValidationUtils.notBlank("  value  ", ERROR_MESSAGE);

        assertValid(result, "  value  ");
    }

    @Test
    void notBlank_withBlankValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notBlank("   ", ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void notBlank_withNullValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notBlank(null, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void notEmpty_withNonEmptyString_returnsValid() {
        Validation<String, String> result = ValidationUtils.notEmpty("value", ERROR_MESSAGE);

        assertValid(result, "value");
    }

    @Test
    void notEmpty_withEmptyString_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.notEmpty("", ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void notEmptyCollection_withNonEmptyCollection_returnsValid() {
        List<Integer> collection = List.of(1, 2, 3);

        Validation<String, List<Integer>> result = ValidationUtils.notEmptyCollection(collection, ERROR_MESSAGE);

        assertValid(result, collection);
    }

    @Test
    void notEmptyCollection_withEmptyCollection_returnsInvalid() {
        Validation<String, List<Integer>> result = ValidationUtils.notEmptyCollection(List.of(), ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void size_withValueWithinRange_returnsValid() {
        Validation<String, String> result = ValidationUtils.size("abcd", 2, 4, ERROR_MESSAGE);

        assertValid(result, "abcd");
    }

    @Test
    void size_withValueOutsideRange_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.size("a", 2, 4, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void minLength_withValueMeetingMinimum_returnsValid() {
        Validation<String, String> result = ValidationUtils.minLength("abc", 3, ERROR_MESSAGE);

        assertValid(result, "abc");
    }

    @Test
    void minLength_withValueBelowMinimum_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.minLength("ab", 3, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void maxLength_withValueWithinLimit_returnsValid() {
        Validation<String, String> result = ValidationUtils.maxLength("abc", 3, ERROR_MESSAGE);

        assertValid(result, "abc");
    }

    @Test
    void maxLength_withValueExceedingLimit_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.maxLength("abcd", 3, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void sizeCollection_withCollectionWithinRange_returnsValid() {
        List<Integer> collection = List.of(1, 2, 3);

        Validation<String, List<Integer>> result = ValidationUtils.sizeCollection(collection, 2, 3, ERROR_MESSAGE);

        assertValid(result, collection);
    }

    @Test
    void sizeCollection_withCollectionOutsideRange_returnsInvalid() {
        Validation<String, List<Integer>> result = ValidationUtils.sizeCollection(List.of(1), 2, 3, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void max_withValueAtUpperBound_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.max(10, 10, ERROR_MESSAGE);

        assertValid(result, 10);
    }

    @Test
    void max_withValueAboveUpperBound_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.max(11, 10, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void min_withValueAtLowerBound_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.min(10, 10, ERROR_MESSAGE);

        assertValid(result, 10);
    }

    @Test
    void min_withValueBelowLowerBound_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.min(9, 10, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void range_withValueWithinBounds_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.range(5, 1, 10, ERROR_MESSAGE);

        assertValid(result, 5);
    }

    @Test
    void range_withValueOutsideBounds_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.range(0, 1, 10, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void positive_integerWithPositiveValue_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.positive(1, ERROR_MESSAGE);

        assertValid(result, 1);
    }

    @Test
    void positive_integerWithZero_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.positive(0, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void positive_longWithPositiveValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.positive(1L, ERROR_MESSAGE);

        assertValid(result, 1L);
    }

    @Test
    void positive_longWithNegativeValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.positive(-1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void positive_doubleWithPositiveValue_returnsValid() {
        Validation<String, Double> result = ValidationUtils.positive(0.1d, ERROR_MESSAGE);

        assertValid(result, 0.1d);
    }

    @Test
    void positive_doubleWithZero_returnsInvalid() {
        Validation<String, Double> result = ValidationUtils.positive(0.0d, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void positiveOrZero_integerWithZero_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.positiveOrZero(0, ERROR_MESSAGE);

        assertValid(result, 0);
    }

    @Test
    void positiveOrZero_integerWithNegativeValue_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.positiveOrZero(-1, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void positiveOrZero_longWithPositiveValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.positiveOrZero(1L, ERROR_MESSAGE);

        assertValid(result, 1L);
    }

    @Test
    void positiveOrZero_longWithNegativeValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.positiveOrZero(-1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void negative_integerWithNegativeValue_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.negative(-1, ERROR_MESSAGE);

        assertValid(result, -1);
    }

    @Test
    void negative_integerWithZero_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.negative(0, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void negative_longWithNegativeValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.negative(-1L, ERROR_MESSAGE);

        assertValid(result, -1L);
    }

    @Test
    void negative_longWithPositiveValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.negative(1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void negativeOrZero_integerWithZero_returnsValid() {
        Validation<String, Integer> result = ValidationUtils.negativeOrZero(0, ERROR_MESSAGE);

        assertValid(result, 0);
    }

    @Test
    void negativeOrZero_integerWithPositiveValue_returnsInvalid() {
        Validation<String, Integer> result = ValidationUtils.negativeOrZero(1, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void negativeOrZero_longWithNegativeValue_returnsValid() {
        Validation<String, Long> result = ValidationUtils.negativeOrZero(-1L, ERROR_MESSAGE);

        assertValid(result, -1L);
    }

    @Test
    void negativeOrZero_longWithPositiveValue_returnsInvalid() {
        Validation<String, Long> result = ValidationUtils.negativeOrZero(1L, ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void pattern_withMatchingValue_returnsValid() {
        Validation<String, String> result = ValidationUtils.pattern("ABC123", "^[A-Z0-9]+$", ERROR_MESSAGE);

        assertValid(result, "ABC123");
    }

    @Test
    void pattern_withNonMatchingValue_returnsInvalid() {
        Validation<String, String> result = ValidationUtils.pattern("abc123", "^[A-Z0-9]+$", ERROR_MESSAGE);

        assertInvalid(result);
    }

    @Test
    void email_withValidEmail_returnsValid() {
        Validation<String, String> result = ValidationUtils.email("test.user+shop@example.com", ERROR_MESSAGE);

        assertValid(result, "test.user+shop@example.com");
    }

    @Test
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
