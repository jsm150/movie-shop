package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("RegisterTheaterCommandHandler 통합 테스트")
class RegisterTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("상영관 등록 시 DB에 값이 저장된다")
    void registerTheater_persistsToDatabase() {
        // given
        RegisterTheaterCommand command = new RegisterTheaterCommand(
                "1관",
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                2,
                3
        );

        // when
        Long theaterId = pipeline.send(command);
        entityManager.flush();
        entityManager.clear();

        // then
        Theater theater = theaterRepository.findById(theaterId).orElseThrow();
        assertThat(theater.getName().getName()).isEqualTo("1관");
        assertThat(theater.getFloor()).isEqualTo(1);
        assertThat(theater.getTheaterType()).isEqualTo(TheaterType.Standard);
        assertThat(theater.getSeats().getSeats()).containsExactly("A1", "A2", "A3", "B1", "B2", "B3");
        assertThat(theater.getSeats().getRowCount()).isEqualTo(2);
        assertThat(theater.getSeats().getColumnCount()).isEqualTo(3);
        assertThat(theater.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("동일한 상영관 이름으로 등록 시 중복을 허용하지 않는다")
    void registerTheater_withDuplicateName_fails() {
        // given
        RegisterTheaterCommand first = new RegisterTheaterCommand(
                "1관",
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                2,
                3
        );
        pipeline.send(first);
        entityManager.flush();
        entityManager.clear();

        RegisterTheaterCommand duplicate = new RegisterTheaterCommand(
                "1관",
                2,
                TheaterType.IMAX,
                List.of("C1", "C2", "C3", "D1", "D2", "D3"),
                2,
                3
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(duplicate))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("'1관' 이름의 상영관이 이미 존재합니다.");

        assertThat(theaterRepository.count()).isEqualTo(1);
    }
}
