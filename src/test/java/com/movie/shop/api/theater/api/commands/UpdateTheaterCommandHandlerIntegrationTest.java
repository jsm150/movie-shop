package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNameDuplicateValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("UpdateTheaterCommandHandler 통합 테스트")
class UpdateTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterNameDuplicateValidator validator;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private TheaterJpaPort theaterJpaPort;

    @Autowired
    private EntityManager entityManager;

    private Theater createAndSaveTheater(String name) {
        Theater theater = Theater.Register(
                validator,
                name,
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                2,
                3
        );

        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();

        return theater;
    }

    @Nested
    @DisplayName("극장 수정 성공 테스트")
    class UpdateTheaterSuccessTest {

        @Test
        @Transactional
        @DisplayName("극장 정보 전체 수정하면 DB에 변경사항이 반영된다")
        void updateTheater_allFields_updatesDatabase() {
            // given
            Theater theater = createAndSaveTheater("1관");
            long theaterId = theater.getId();

            UpdateTheaterCommand command = new UpdateTheaterCommand(
                    theaterId,
                    "2관",
                    2,
                    TheaterType.IMAX,
                    List.of("C1", "C2", "D1", "D2"),
                    2,
                    2
            );

            // when
            Long resultId = pipeline.send(command);
            entityManager.flush();
            entityManager.clear();

            // then
            Theater updated = theaterJpaPort.findById(theaterId).orElseThrow();
            assertThat(resultId).isEqualTo(theaterId);
            assertThat(updated.getName().getName()).isEqualTo("2관");
            assertThat(updated.getFloor()).isEqualTo(2);
            assertThat(updated.getTheaterType()).isEqualTo(TheaterType.IMAX);
            assertThat(updated.getSeats().getSeats()).containsExactly("C1", "C2", "D1", "D2");
            assertThat(updated.getSeats().getRowCount()).isEqualTo(2);
            assertThat(updated.getSeats().getColumnCount()).isEqualTo(2);
        }

        @Test
        @Transactional
        @DisplayName("동일한 상영관 이름으로 수정하면 중복 검증을 스킵하고 성공한다")
        void updateTheater_withSameName_successWithoutDuplicateCheck() {
            // given
            Theater theater = createAndSaveTheater("1관");
            long theaterId = theater.getId();

            UpdateTheaterCommand command = new UpdateTheaterCommand(
                    theaterId,
                    "1관",
                    3,
                    TheaterType.Premium,
                    List.of("E1", "E2", "E3", "F1", "F2", "F3"),
                    2,
                    3
            );

            // when
            pipeline.send(command);
            entityManager.flush();
            entityManager.clear();

            // then
            Theater updated = theaterJpaPort.findById(theaterId).orElseThrow();
            assertThat(updated.getName().getName()).isEqualTo("1관");
            assertThat(updated.getFloor()).isEqualTo(3);
            assertThat(updated.getTheaterType()).isEqualTo(TheaterType.Premium);
        }
    }

    @Nested
    @DisplayName("극장 이름 수정 테스트")
    class UpdateTheaterNameTest {

        @Test
        @Transactional
        @DisplayName("이미 존재하는 상영관 이름으로 변경하면 실패한다")
        void updateTheater_withDuplicateName_throwsException() {
            // given
            createAndSaveTheater("1관");
            Theater theater2 = createAndSaveTheater("2관");
            long theater2Id = theater2.getId();

            UpdateTheaterCommand command = new UpdateTheaterCommand(
                    theater2Id,
                    "1관",
                    2,
                    TheaterType.Standard,
                    List.of("C1", "C2", "C3", "D1", "D2", "D3"),
                    2,
                    3
            );

            // when & then
            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("'1관' 이름의 상영관이 이미 존재합니다.");
        }
    }

    @Nested
    @DisplayName("극장 수정 실패 테스트")
    class UpdateTheaterFailureTest {

        @Test
        @Transactional
        @DisplayName("존재하지 않는 상영관 ID로 수정하면 예외가 발생한다")
        void updateTheater_withNonExistentId_throwsException() {
            // given
            long nonExistentTheaterId = 999999L;

            UpdateTheaterCommand command = new UpdateTheaterCommand(
                    nonExistentTheaterId,
                    "존재하지 않는 상영관",
                    1,
                    TheaterType.Standard,
                    List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                    2,
                    3
            );

            // when & then
            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
        }
    }
}
