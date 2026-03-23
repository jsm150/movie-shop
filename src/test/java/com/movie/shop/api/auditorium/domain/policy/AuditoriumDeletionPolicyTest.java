package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumName;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumSeats;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumScreeningLinkStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditoriumDeletionPolicy 단위 테스트")
class AuditoriumDeletionPolicyTest {

    @Test
    @DisplayName("DELETE 시 차단 상영이 존재하면 예외가 발생한다")
    void validateCanDelete_whenBlockingScreeningExists_throwsException() throws Exception {
        Auditorium auditorium = createAuditorium(1L, true);
        AuditoriumDeletionPolicy policy = new AuditoriumDeletionPolicy(
                new AuditoriumScreeningLinkStatus(true)
        );

        assertThatThrownBy(() -> policy.validateCanDelete(auditorium))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("DELETE 시 차단 상영이 없으면 예외가 발생하지 않는다")
    void validateCanDelete_whenNoBlockingScreening_doesNotThrow() throws Exception {
        Auditorium auditorium = createAuditorium(1L, true);
        AuditoriumDeletionPolicy policy = new AuditoriumDeletionPolicy(
                new AuditoriumScreeningLinkStatus(false)
        );

        assertThatCode(() -> policy.validateCanDelete(auditorium))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("생성 시 상영 연결 상태가 null이면 예외가 발생한다")
    void constructor_whenScreeningLinkStatusNull_throwsException() {
        assertThatThrownBy(() -> new AuditoriumDeletionPolicy(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("상영 연결 상태는 필수입니다.");
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
