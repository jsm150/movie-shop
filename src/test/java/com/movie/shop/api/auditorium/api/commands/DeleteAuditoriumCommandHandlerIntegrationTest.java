package com.movie.shop.api.auditorium.api.commands;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("DeleteAuditoriumCommandHandler 통합 테스트")
class DeleteAuditoriumCommandHandlerIntegrationTest extends AuditoriumCommandIntegrationTestSupport {

    @Test
    @DisplayName("존재하는 상영관을 삭제하면 DB에서 제거된다")
    @Transactional
    void deleteAuditorium_success() {
        Theater theater = createAndSaveTheater("강남점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");

        assertThat(auditoriumJpaPort.findById(auditorium.getId())).isPresent();

        pipeline.send(new DeleteAuditoriumCommand(auditorium.getId()));
        flushAndClear();

        assertThat(auditoriumJpaPort.findById(auditorium.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 상영관 ID로 삭제를 요청하면 실패한다")
    @Transactional
    void deleteAuditorium_withNonExistentId_fails() {
        assertThatThrownBy(() -> pipeline.send(new DeleteAuditoriumCommand(999999L)))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("차단 상영이 존재하는 상영관을 삭제하면 실패한다")
    @Transactional
    void deleteAuditorium_withBlockingScreening_fails() {
        Theater theater = createAndSaveTheater("홍대점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");
        insertBlockingScreening(auditorium.getId());
        flushAndClear();

        assertThatThrownBy(() -> pipeline.send(new DeleteAuditoriumCommand(auditorium.getId())))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");

        assertThat(auditoriumJpaPort.findById(auditorium.getId())).isPresent();
    }
}
