package com.movie.shop.api.auditorium.domain.aggregate.newtype;

import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumName;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditoriumName 단위 테스트")
class AuditoriumNameTest {

    private AuditoriumNameUniquenessCondition uniqueCondition;
    private AuditoriumNameUniquenessCondition duplicateCondition;

    @BeforeEach
    void setUp() {
        uniqueCondition = new AuditoriumNameUniquenessCondition(true);
        duplicateCondition = new AuditoriumNameUniquenessCondition(false);
    }

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 상영관 이름으로 생성 성공한다")
        void createNew_withValidName_success() {
            String validName = "1관";

            AuditoriumName auditoriumName = AuditoriumName.createNew(
                    validName,
                    uniqueCondition
            );

            assertThat(auditoriumName.getName()).isEqualTo(validName);
        }

        @Test
        @DisplayName("null 이름으로 생성 실패한다")
        void createNew_withNullName_fail() {
            assertThatThrownBy(() -> AuditoriumName.createNew(
                    null,
                    uniqueCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 생성 실패한다")
        void createNew_withEmptyName_fail() {
            assertThatThrownBy(() -> AuditoriumName.createNew(
                    "",
                    uniqueCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 생성 실패한다")
        void createNew_withTooLongName_fail() {
            String longName = "a".repeat(51);

            assertThatThrownBy(() -> AuditoriumName.createNew(
                    longName,
                    uniqueCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복 조건이 없으면 생성 실패한다")
        void createNew_withNullCondition_fail() {
            assertThatThrownBy(() -> AuditoriumName.createNew("1관", null))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름 중복 조건은 필수입니다.");
        }

        @Test
        @DisplayName("동일 영화관에서 중복 이름이면 생성 실패한다")
        void createNew_withDuplicateNameInSameTheater_fail() {
            assertThatThrownBy(() -> AuditoriumName.createNew(
                    "1관",
                    duplicateCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private AuditoriumName existingName;

        @BeforeEach
        void setUp() {
            existingName = AuditoriumName.createNew(
                    "기존 상영관",
                    uniqueCondition
            );
        }

        @Test
        @DisplayName("다른 유효한 이름으로 변경 성공한다")
        void createFrom_withDifferentValidName_success() {
            String newName = "새 상영관";

            AuditoriumName auditoriumName = AuditoriumName.createFrom(
                    existingName,
                    newName,
                    uniqueCondition
            );

            assertThat(auditoriumName.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("동일한 이름으로 변경하면 중복 조건과 무관하게 성공한다")
        void createFrom_withSameName_successWithoutDuplicateCheck() {
            String sameName = "기존 상영관";

            AuditoriumName auditoriumName = AuditoriumName.createFrom(
                    existingName,
                    sameName,
                    duplicateCondition
            );

            assertThat(auditoriumName.getName()).isEqualTo(sameName);
        }

        @Test
        @DisplayName("null 이름으로 변경 실패한다")
        void createFrom_withNullName_fail() {
            assertThatThrownBy(() -> AuditoriumName.createFrom(
                    existingName,
                    null,
                    uniqueCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 변경 실패한다")
        void createFrom_withEmptyName_fail() {
            assertThatThrownBy(() -> AuditoriumName.createFrom(
                    existingName,
                    "",
                    uniqueCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 변경 실패한다")
        void createFrom_withTooLongName_fail() {
            String longName = "a".repeat(51);

            assertThatThrownBy(() -> AuditoriumName.createFrom(
                    existingName,
                    longName,
                    uniqueCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("동일 영화관에서 중복된 이름으로 변경 실패한다")
        void createFrom_withDuplicateName_fail() {
            assertThatThrownBy(() -> AuditoriumName.createFrom(
                    existingName,
                    "중복 상영관",
                    duplicateCondition
            ))
                    .isInstanceOf(AuditoriumDomainException.class)
                    .hasMessageContaining("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }
}
