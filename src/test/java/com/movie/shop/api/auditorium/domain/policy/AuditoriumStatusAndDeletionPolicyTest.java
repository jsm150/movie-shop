package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumStatusChange;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumName;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumSeats;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumScreeningLinkStatus;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterActivationStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditoriumStatusAndDeletionPolicy 단위 테스트")
class AuditoriumStatusAndDeletionPolicyTest {

    @Test
    @DisplayName("DEACTIVATE 시 차단 상영이 존재하면 예외가 발생한다")
    void validateCanChangeActive_whenDeactivateAndBlockingScreeningExists_throwsException() throws Exception {
        Auditorium auditorium = createAuditorium(1L, true);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(true),
                Optional.empty()
        );

        assertThatThrownBy(() -> policy.validateCanChangeStatus(auditorium, AuditoriumStatusChange.DEACTIVATE))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("DEACTIVATE 시 차단 상영이 없으면 예외가 발생하지 않는다")
    void validateCanChangeActive_whenDeactivateAndNoBlockingScreening_doesNotThrow() throws Exception {
        Auditorium auditorium = createAuditorium(1L, true);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(false),
                Optional.empty()
        );

        assertThatCode(() -> policy.validateCanChangeStatus(auditorium, AuditoriumStatusChange.DEACTIVATE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 비활성인 상영관은 DEACTIVATE 시 차단 상영이 있어도 통과한다")
    void validateCanChangeActive_whenAlreadyInactiveAndDeactivate_doesNotThrow() throws Exception {
        Auditorium auditorium = createAuditorium(1L, false);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(true),
                Optional.empty()
        );

        assertThatCode(() -> policy.validateCanChangeStatus(auditorium, AuditoriumStatusChange.DEACTIVATE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ACTIVATE 시 연결 영화관이 활성 상태면 통과한다")
    void validateCanChangeActive_whenActivateAndTheaterActive_doesNotThrow() throws Exception {
        Auditorium auditorium = createAuditorium(1L, false);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(false),
                Optional.of(new AuditoriumTheaterActivationStatus(true))
        );

        assertThatCode(() -> policy.validateCanChangeStatus(auditorium, AuditoriumStatusChange.ACTIVATE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ACTIVATE 시 연결 영화관 정보가 없으면 예외가 발생한다")
    void validateCanChangeActive_whenActivateAndTheaterMissing_throwsException() throws Exception {
        Auditorium auditorium = createAuditorium(1L, false);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(false),
                Optional.empty()
        );

        assertThatThrownBy(() -> policy.validateCanChangeStatus(auditorium, AuditoriumStatusChange.ACTIVATE))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("영화관 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("ACTIVATE 시 연결 영화관이 비활성이면 예외가 발생한다")
    void validateCanChangeActive_whenActivateAndTheaterInactive_throwsException() throws Exception {
        Auditorium auditorium = createAuditorium(1L, false);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(false),
                Optional.of(new AuditoriumTheaterActivationStatus(false))
        );

        assertThatThrownBy(() -> policy.validateCanChangeStatus(auditorium, AuditoriumStatusChange.ACTIVATE))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("비활성화된 영화관의 상영관은 활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("DELETE 시 차단 상영이 존재하면 예외가 발생한다")
    void validateCanDelete_whenBlockingScreeningExists_throwsException() throws Exception {
        Auditorium auditorium = createAuditorium(1L, true);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(true),
                Optional.empty()
        );

        assertThatThrownBy(() -> policy.validateCanDelete(auditorium))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("DELETE 시 차단 상영이 없으면 예외가 발생하지 않는다")
    void validateCanDelete_whenNoBlockingScreening_doesNotThrow() throws Exception {
        Auditorium auditorium = createAuditorium(1L, true);
        AuditoriumStatusAndDeletionPolicy policy = new AuditoriumStatusAndDeletionPolicy(
                new AuditoriumScreeningLinkStatus(false),
                Optional.empty()
        );

        assertThatCode(() -> policy.validateCanDelete(auditorium))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("생성 시 상영 연결 상태가 null이면 예외가 발생한다")
    void constructor_whenScreeningLinkStatusNull_throwsException() {
        assertThatThrownBy(() -> new AuditoriumStatusAndDeletionPolicy(null, Optional.empty()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("상영 연결 상태는 필수입니다.");
    }

    @Test
    @DisplayName("생성 시 영화관 활성 상태 정보가 null이면 예외가 발생한다")
    void constructor_whenTheaterActivationStatusNull_throwsException() {
        assertThatThrownBy(() -> new AuditoriumStatusAndDeletionPolicy(new AuditoriumScreeningLinkStatus(false), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("영화관 활성 상태 정보는 필수입니다.");
    }

    private Auditorium createAuditorium(long auditoriumId, boolean active) throws Exception {
        var constructor = Auditorium.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Auditorium auditorium = constructor.newInstance();

        Field idField = Auditorium.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(auditorium, auditoriumId);

        Field theaterIdField = Auditorium.class.getDeclaredField("theaterId");
        theaterIdField.setAccessible(true);
        theaterIdField.set(auditorium, 1L);

        Field nameField = Auditorium.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(auditorium, new AuditoriumName("정책테스트관"));

        Field floorField = Auditorium.class.getDeclaredField("floor");
        floorField.setAccessible(true);
        floorField.set(auditorium, 1);

        Field typeField = Auditorium.class.getDeclaredField("auditoriumType");
        typeField.setAccessible(true);
        typeField.set(auditorium, AuditoriumType.Standard);

        Field seatsField = Auditorium.class.getDeclaredField("seats");
        seatsField.setAccessible(true);
        seatsField.set(auditorium, AuditoriumSeats.create(List.of("A1", "A2"), 1, 2).get());

        Field activeField = Auditorium.class.getDeclaredField("active");
        activeField.setAccessible(true);
        activeField.set(auditorium, active);

        return auditorium;
    }
}
