package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterExistenceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditoriumTheaterExistencePolicy 단위 테스트")
class AuditoriumTheaterExistencePolicyTest {

    @Test
    @DisplayName("연결 영화관이 존재하면 등록 검증이 통과한다")
    void validateCanRegister_whenTheaterExists_doesNotThrow() {
        AuditoriumTheaterExistencePolicy policy = new AuditoriumTheaterExistencePolicy(
                new AuditoriumTheaterExistenceStatus(true)
        );

        assertThatCode(policy::validateCanRegister).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("연결 영화관이 존재하지 않으면 등록 검증이 실패한다")
    void validateCanRegister_whenTheaterMissing_throwsException() {
        AuditoriumTheaterExistencePolicy policy = new AuditoriumTheaterExistencePolicy(
                new AuditoriumTheaterExistenceStatus(false)
        );

        assertThatThrownBy(policy::validateCanRegister)
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("존재하지 않는 영화관");
    }

    @Test
    @DisplayName("생성 시 영화관 상태 정보가 null이면 예외가 발생한다")
    void constructor_whenTheaterActivationStatusNull_throwsException() {
        assertThatThrownBy(() -> new AuditoriumTheaterExistencePolicy(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("영화관 존재 상태 정보는 필수입니다.");
    }
}
