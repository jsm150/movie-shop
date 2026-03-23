package com.movie.shop.api.auditorium.api.commands;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumStatusChange;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("ChangeStatusAuditoriumCommandHandler 통합 테스트")
class ChangeStatusAuditoriumCommandHandlerIntegrationTest extends AuditoriumCommandIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("활성화된 상영관을 비활성화한다")
    void changeStatus_deactivate_success() {
        Theater theater = createAndSaveTheater("강남점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");

        pipeline.send(new ChangeStatusAuditoriumCommand(auditorium.getId(), AuditoriumStatusChange.DEACTIVATE));
        flushAndClear();

        Auditorium updated = auditoriumRepository.getById(auditorium.getId());
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("차단 상영이 존재하는 상영관은 비활성화할 수 없다")
    void changeStatus_withBlockingScreening_fails() {
        Theater theater = createAndSaveTheater("홍대점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");
        insertBlockingScreening(auditorium.getId());
        flushAndClear();

        assertThatThrownBy(() -> pipeline.send(
                new ChangeStatusAuditoriumCommand(auditorium.getId(), AuditoriumStatusChange.DEACTIVATE)
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("비활성화된 영화관의 상영관은 활성화할 수 없다")
    void changeStatus_activateWhenTheaterInactive_fails() {
        Theater theater = createAndSaveTheater("잠실점", false);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");

        pipeline.send(new ChangeStatusAuditoriumCommand(auditorium.getId(), AuditoriumStatusChange.DEACTIVATE));
        flushAndClear();

        assertThatThrownBy(() -> pipeline.send(
                new ChangeStatusAuditoriumCommand(auditorium.getId(), AuditoriumStatusChange.ACTIVATE)
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("활성화할 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("비활성화된 상영관을 활성화한다")
    void changeStatus_activate_success() {
        Theater theater = createAndSaveTheater("수원점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");

        pipeline.send(new ChangeStatusAuditoriumCommand(auditorium.getId(), AuditoriumStatusChange.DEACTIVATE));
        pipeline.send(new ChangeStatusAuditoriumCommand(auditorium.getId(), AuditoriumStatusChange.ACTIVATE));
        flushAndClear();

        Auditorium updated = auditoriumRepository.getById(auditorium.getId());
        assertThat(updated.isActive()).isTrue();
    }
}
