package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.status.TheaterAuditoriumLinkStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheaterAuditoriumLinkProtectionPolicyTest {

    @Test
    @DisplayName("연결된 상영관이 존재하면 비활성화 검증 시 예외가 발생한다")
    void validateCanChangeActive_whenDeactivateAndLinkedAuditoriumExists_throwsException() throws Exception {
        Theater theater = createTheater(1L, true);
        TheaterAuditoriumLinkProtectionPolicy policy =
                new TheaterAuditoriumLinkProtectionPolicy(new TheaterAuditoriumLinkStatus(true));

        assertThatThrownBy(() -> policy.validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("연결된 상영관이 없으면 비활성화 검증에 성공한다")
    void validateCanChangeActive_whenDeactivateAndNoLinkedAuditorium_doesNotThrow() throws Exception {
        Theater theater = createTheater(1L, true);
        TheaterAuditoriumLinkProtectionPolicy policy =
                new TheaterAuditoriumLinkProtectionPolicy(new TheaterAuditoriumLinkStatus(false));

        assertThatCode(() -> policy.validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 비활성 상태에서 비활성화를 요청하면 통과한다")
    void validateCanChangeActive_whenAlreadyInactive_doesNotThrow() throws Exception {
        Theater theater = createTheater(1L, false);
        TheaterAuditoriumLinkProtectionPolicy policy =
                new TheaterAuditoriumLinkProtectionPolicy(new TheaterAuditoriumLinkStatus(true));

        assertThatCode(() -> policy.validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("연결된 상영관이 존재하면 삭제 검증 시 예외가 발생한다")
    void validateCanDelete_whenLinkedAuditoriumExists_throwsException() throws Exception {
        Theater theater = createTheater(1L, true);
        TheaterAuditoriumLinkProtectionPolicy policy =
                new TheaterAuditoriumLinkProtectionPolicy(new TheaterAuditoriumLinkStatus(true));

        assertThatThrownBy(() -> policy.validateCanDelete(theater))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("연결된 상영관이 없으면 삭제 검증에 성공한다")
    void validateCanDelete_whenNoLinkedAuditorium_doesNotThrow() throws Exception {
        Theater theater = createTheater(1L, true);
        TheaterAuditoriumLinkProtectionPolicy policy =
                new TheaterAuditoriumLinkProtectionPolicy(new TheaterAuditoriumLinkStatus(false));

        assertThatCode(() -> policy.validateCanDelete(theater))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("정책 생성 시 연결 상태가 null이면 예외가 발생한다")
    void constructor_whenLinkStatusNull_throwsException() {
        assertThatThrownBy(() -> new TheaterAuditoriumLinkProtectionPolicy(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("영화관-상영관 연결 상태는 필수입니다.");
    }

    private Theater createTheater(long theaterId, boolean active) throws Exception {
        Constructor<Theater> constructor = Theater.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Theater theater = constructor.newInstance();

        Field idField = Theater.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(theater, theaterId);

        Field activeField = Theater.class.getDeclaredField("active");
        activeField.setAccessible(true);
        activeField.set(theater, active);

        return theater;
    }
}
