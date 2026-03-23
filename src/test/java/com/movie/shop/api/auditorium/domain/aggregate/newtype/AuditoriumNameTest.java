package com.movie.shop.api.auditorium.domain.aggregate.newtype;

import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumName;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditoriumName 단위 테스트")
class AuditoriumNameTest {

    @Mock
    private AuditoriumNameDuplicatePolicy mockValidator;

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 상영관 이름으로 생성 성공한다")
        void createNew_withValidName_success() {
            String validName = "1관";

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(validName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(validName);
            verify(mockValidator).validateNotDuplicate();
        }

        @Test
        @DisplayName("null 이름으로 생성 실패한다")
        void createNew_withNullName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(null, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 생성 실패한다")
        void createNew_withEmptyName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew("", mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 생성 실패한다")
        void createNew_withTooLongName_fail() {
            String longName = "a".repeat(51);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(longName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("동일 theaterId에서 중복 이름이면 생성 실패한다")
        void createNew_withDuplicateNameInSameTheater_fail() {
            doThrow(new AuditoriumDomainException("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다."))
                    .when(mockValidator)
                    .validateNotDuplicate();

            assertThatThrownBy(() -> AuditoriumName.createNew("1관", mockValidator))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }

        @Test
        @DisplayName("다른 theaterId면 같은 이름도 생성 가능하다")
        void createNew_withSameNameInDifferentTheater_success() {
            String sameName = "1관";

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(sameName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(sameName);
            verify(mockValidator).validateNotDuplicate();
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private AuditoriumName existingName;

        @BeforeEach
        void setUp() {
            existingName = AuditoriumName.createNew("기존 상영관", mockValidator).get();
        }

        @Test
        @DisplayName("다른 유효한 이름으로 변경 성공한다")
        void createFrom_withDifferentValidName_success() {
            String newName = "새 상영관";

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(existingName, newName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("동일한 이름으로 변경하면 중복 검증을 스킵하고 성공한다")
        void createFrom_withSameName_successWithoutDuplicateCheck() {
            String sameName = "기존 상영관";

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(existingName, sameName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(sameName);
        }

        @Test
        @DisplayName("null 이름으로 변경 실패한다")
        void createFrom_withNullName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(existingName, null, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 변경 실패한다")
        void createFrom_withEmptyName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(existingName, "", mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 변경 실패한다")
        void createFrom_withTooLongName_fail() {
            String longName = "a".repeat(51);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(existingName, longName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("동일 theaterId에서 중복된 이름으로 변경 실패한다")
        void createFrom_withDuplicateName_fail() {
            doThrow(new AuditoriumDomainException("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다."))
                    .when(mockValidator)
                    .validateNotDuplicate();

            assertThatThrownBy(() -> AuditoriumName.createFrom(existingName, "중복 상영관", mockValidator))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }
}
