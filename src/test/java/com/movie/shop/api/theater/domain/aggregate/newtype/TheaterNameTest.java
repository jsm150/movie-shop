package com.movie.shop.api.theater.domain.aggregate.newtype;

import com.movie.shop.api.theater.domain.aggregate.TheaterName;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("TheaterName 단위 테스트")
class TheaterNameTest {

    @Mock
    private TheaterNamePolicy mockValidator;

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 영화관 이름으로 생성 성공한다")
        void createNew_withValidName_success() {
            String validName = "강남점";

            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(validName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(validName);
        }

        @Test
        @DisplayName("null 이름으로 생성 실패한다")
        void createNew_withNullName_fail() {
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(null, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 생성 실패한다")
        void createNew_withEmptyName_fail() {
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew("", mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 생성 실패한다")
        void createNew_withTooLongName_fail() {
            String longName = "a".repeat(51);

            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(longName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 이름으로 생성 실패한다")
        void createNew_withDuplicateName_fail() {
            doThrow(new TheaterDomainException("동일한 이름의 영화관이 이미 존재합니다."))
                    .when(mockValidator)
                    .validateNotDuplicate(anyString());

            assertThatThrownBy(() -> TheaterName.createNew("강남점", mockValidator))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("동일한 이름의 영화관이 이미 존재합니다.");
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private TheaterName existingName;

        @BeforeEach
        void setUp() {
            existingName = TheaterName.createNew("기존영화관", mockValidator).get();
        }

        @Test
        @DisplayName("다른 유효한 이름으로 변경 성공한다")
        void createFrom_withDifferentValidName_success() {
            String newName = "새영화관";

            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, newName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("동일한 이름으로 변경하면 중복 검증 스킵하고 성공한다")
        void createFrom_withSameName_successWithoutDuplicateCheck() {
            String sameName = "기존영화관";

            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, sameName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(sameName);
        }

        @Test
        @DisplayName("중복된 이름으로 변경 실패한다")
        void createFrom_withDuplicateName_fail() {
            doThrow(new TheaterDomainException("동일한 이름의 영화관이 이미 존재합니다."))
                    .when(mockValidator)
                    .validateNotDuplicate(anyString());

            assertThatThrownBy(() -> TheaterName.createFrom(existingName, "중복영화관", mockValidator))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("동일한 이름의 영화관이 이미 존재합니다.");
        }
    }
}
