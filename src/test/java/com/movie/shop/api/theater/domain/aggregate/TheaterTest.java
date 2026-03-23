package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.TheaterAuditoriumLinkProtectionPolicy;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Theater 단위 테스트")
class TheaterTest {

    @Mock
    private TheaterNamePolicy validator;

    @Mock
    private TheaterAuditoriumLinkProtectionPolicy theaterAuditoriumLinkProtectionPolicy;

    private String validName;

    @BeforeEach
    void setUp() {
        validName = "강남점";
    }

    @Test
    @DisplayName("유효한 데이터로 등록 성공한다")
    void register_withValidData_success() {
        Theater theater = Theater.register(validator, validName);

        assertThat(theater.getName().getName()).isEqualTo(validName);
        assertThat(theater.isActive()).isTrue();
    }

    @Test
    @DisplayName("빈 이름으로 등록 실패한다")
    void register_withBlankName_fail() {
        assertThatThrownBy(() -> Theater.register(validator, ""))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("영화관 이름은 필수입니다.");
    }

    @Test
    @DisplayName("등록된 영화관 이름을 수정한다")
    void updateName_withValidName_success() {
        Theater theater = Theater.register(validator, validName);

        theater.updateName(validator, "홍대점");

        assertThat(theater.getName().getName()).isEqualTo("홍대점");
    }

    @Test
    @DisplayName("중복된 이름으로 수정하면 실패한다")
    void updateName_withDuplicateName_fail() {
        Theater theater = Theater.register(validator, validName);

        doThrow(new TheaterDomainException("동일한 이름의 영화관이 이미 존재합니다."))
                .when(validator)
                .validateNotDuplicate();

        assertThatThrownBy(() -> theater.updateName(validator, "홍대점"))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("이미 존재합니다.");
    }

    @Test
    @DisplayName("DEACTIVATE 요청 시 정책에서 차단되면 예외가 발생한다")
    void changeActive_whenDeactivateBlocked_throwsException() {
        Theater theater = Theater.register(validator, validName);
        doThrow(new TheaterDomainException("차단"))
                .when(theaterAuditoriumLinkProtectionPolicy)
                .validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE);

        assertThatThrownBy(() -> theater.changeActive(TheaterActiveChange.DEACTIVATE, theaterAuditoriumLinkProtectionPolicy))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("차단");
    }

    @Test
    @DisplayName("DEACTIVATE 요청 시 정책 검증을 통과하면 비활성 상태로 변경된다")
    void changeActive_whenDeactivateAllowed_becomesInactive() {
        Theater theater = Theater.register(validator, validName);

        theater.changeActive(TheaterActiveChange.DEACTIVATE, theaterAuditoriumLinkProtectionPolicy);

        assertThat(theater.isActive()).isFalse();
    }

    @Test
    @DisplayName("ACTIVATE 요청하면 정책 검증 없이 활성 상태로 변경된다")
    void changeActive_whenActivate_skipsPolicyValidation() throws Exception {
        Theater theater = Theater.register(validator, validName);
        setActive(theater, false);

        theater.changeActive(TheaterActiveChange.ACTIVATE, theaterAuditoriumLinkProtectionPolicy);

        assertThat(theater.isActive()).isTrue();
        verifyNoInteractions(theaterAuditoriumLinkProtectionPolicy);
    }

    @Test
    @DisplayName("상태 변경 요청이 null이면 예외가 발생한다")
    void changeActive_whenStateNull_throwsException() {
        Theater theater = Theater.register(validator, validName);

        assertThatThrownBy(() -> theater.changeActive(null, theaterAuditoriumLinkProtectionPolicy))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("활성 상태는 필수");
    }

    @Test
    @DisplayName("정책이 null이면 상태 변경 시 예외가 발생한다")
    void changeActive_whenPolicyNull_throwsException() {
        Theater theater = Theater.register(validator, validName);

        assertThatThrownBy(() -> theater.changeActive(TheaterActiveChange.DEACTIVATE, null))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("정책은 필수");
    }

    private void setActive(Theater theater, boolean active) throws Exception {
        Field activeField = Theater.class.getDeclaredField("active");
        activeField.setAccessible(true);
        activeField.set(theater, active);
    }
}
