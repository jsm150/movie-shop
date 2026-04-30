package com.movie.shop.api.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEmailConverterTest {

    private final UserEmailConverter converter = new UserEmailConverter();

    @Test
    @DisplayName("DB null 이메일은 빈 Optional로 변환한다")
    void convertToEntityAttribute_withNull_returnsEmptyOptional() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
    }

    @Test
    @DisplayName("DB 공백 이메일은 영속화 데이터 오류로 드러낸다")
    void convertToEntityAttribute_withBlankEmail_fails() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("저장된 사용자 이메일 데이터가 올바르지 않습니다.")
                .hasCauseInstanceOf(UserDomainException.class);
    }

    @Test
    @DisplayName("저장할 이메일 Optional이 null이면 영속화 상태 오류로 드러낸다")
    void convertToDatabaseColumn_withNullOptional_fails() {
        assertThatThrownBy(() -> converter.convertToDatabaseColumn(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("사용자 이메일 저장 상태가 올바르지 않습니다.");
    }
}
