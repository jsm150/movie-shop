package com.movie.shop.api.auditorium.api.commands;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("UpdateAuditoriumCommandHandler 통합 테스트")
class UpdateAuditoriumCommandHandlerIntegrationTest extends AuditoriumCommandIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("상영관 정보를 수정하면 DB에 변경사항이 반영된다")
    void updateAuditorium_success() {
        Theater theater = createAndSaveTheater("강남점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");

        Long resultId = pipeline.send(new UpdateAuditoriumCommand(
                auditorium.getId(),
                "2관",
                2,
                AuditoriumType.IMAX,
                List.of("A1", "A2", "B1", "B2"),
                2,
                2
        ));
        flushAndClear();

        Auditorium updated = auditoriumRepository.getById(auditorium.getId());
        assertThat(resultId).isEqualTo(auditorium.getId());
        assertThat(updated.getName().getName()).isEqualTo("2관");
        assertThat(updated.getFloor()).isEqualTo(2);
        assertThat(updated.getAuditoriumType()).isEqualTo(AuditoriumType.IMAX);
        assertThat(updated.getSeats().getSeats()).containsExactly("A1", "A2", "B1", "B2");
    }

    @Test
    @Transactional
    @DisplayName("동일한 상영관 이름으로 수정하면 성공한다")
    void updateAuditorium_withSameName_success() {
        Theater theater = createAndSaveTheater("신촌점", true);
        Auditorium auditorium = createAndSaveAuditorium(theater.getId(), "1관");

        Long resultId = pipeline.send(new UpdateAuditoriumCommand(
                auditorium.getId(),
                "1관",
                3,
                AuditoriumType.Premium,
                List.of("A1", "A2", "B1", "B2"),
                2,
                2
        ));
        flushAndClear();

        Auditorium updated = auditoriumRepository.getById(auditorium.getId());
        assertThat(resultId).isEqualTo(auditorium.getId());
        assertThat(updated.getName().getName()).isEqualTo("1관");
        assertThat(updated.getFloor()).isEqualTo(3);
        assertThat(updated.getAuditoriumType()).isEqualTo(AuditoriumType.Premium);
        assertThat(updated.getSeats().getSeats()).containsExactly("A1", "A2", "B1", "B2");
    }

    @Test
    @Transactional
    @DisplayName("동일 영화관 내 중복된 이름으로 수정하면 실패한다")
    void updateAuditorium_withDuplicateName_fails() {
        Theater theater = createAndSaveTheater("홍대점", true);
        createAndSaveAuditorium(theater.getId(), "1관");
        Auditorium target = createAndSaveAuditorium(theater.getId(), "2관");

        assertThatThrownBy(() -> pipeline.send(new UpdateAuditoriumCommand(
                target.getId(),
                "1관",
                1,
                AuditoriumType.Standard,
                List.of("A1", "A2"),
                1,
                2
        )))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("이미 존재합니다.");
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 상영관 ID로 수정하면 실패한다")
    void updateAuditorium_withNonExistentId_fails() {
        assertThatThrownBy(() -> pipeline.send(new UpdateAuditoriumCommand(
                999999L,
                "없는상영관",
                1,
                AuditoriumType.Standard,
                List.of("A1", "A2"),
                1,
                2
        )))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
    }
}
