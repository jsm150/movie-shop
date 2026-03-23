package com.movie.shop.api.auditorium.api.commands;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
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
@DisplayName("RegisterAuditoriumCommandHandler 통합 테스트")
class RegisterAuditoriumCommandHandlerIntegrationTest extends AuditoriumCommandIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("상영관을 등록하면 DB에 값이 저장된다")
    void registerAuditorium_persistsToDatabase() {
        Theater theater = createAndSaveTheater("강남점", true);

        Long auditoriumId = pipeline.send(new RegisterAuditoriumCommand(
                theater.getId(),
                "1관",
                1,
                com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType.Standard,
                List.of("A1", "A2"),
                1,
                2
        ));
        flushAndClear();

        Auditorium auditorium = auditoriumJpaPort.findById(auditoriumId).orElseThrow();
        assertThat(auditorium.getTheaterId()).isEqualTo(theater.getId());
        assertThat(auditorium.getName().getName()).isEqualTo("1관");
        assertThat(auditorium.getFloor()).isEqualTo(1);
        assertThat(auditorium.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 영화관에 상영관 등록을 요청하면 실패한다")
    void registerAuditorium_withMissingTheater_fails() {
        assertThatThrownBy(() -> pipeline.send(new RegisterAuditoriumCommand(
                999999L,
                "1관",
                1,
                com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType.Standard,
                List.of("A1", "A2"),
                1,
                2
        )))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("존재하지 않는 영화관에는 상영관을 등록할 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("동일 영화관 내 중복된 상영관 이름으로 등록하면 실패한다")
    void registerAuditorium_withDuplicateName_fails() {
        Theater theater = createAndSaveTheater("홍대점", true);
        pipeline.send(new RegisterAuditoriumCommand(
                theater.getId(),
                "1관",
                1,
                com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType.Standard,
                List.of("A1", "A2"),
                1,
                2
        ));
        flushAndClear();

        assertThatThrownBy(() -> pipeline.send(new RegisterAuditoriumCommand(
                theater.getId(),
                "1관",
                2,
                com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType.IMAX,
                List.of("A1", "A2"),
                1,
                2
        )))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("이미 존재합니다.");
    }
}
