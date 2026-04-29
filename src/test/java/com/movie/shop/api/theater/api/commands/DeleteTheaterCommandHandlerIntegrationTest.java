package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("DeleteTheaterCommandHandler 통합 테스트")
class DeleteTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterJpaPort theaterJpaPort;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Theater createAndSaveTheater(String name) {
        Theater theater = Theater.register(name, new TheaterNameUniquenessCondition(true));
        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();
        return theater;
    }

    private void insertAuditorium(long theaterId, String name) {
        jdbcTemplate.update(
                """
                INSERT INTO auditorium
                (theater_id, name, floor, auditorium_type, is_active, seats, row_count, column_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                theaterId,
                name,
                1,
                "Standard",
                true,
                "[\"A1\",\"A2\"]",
                1,
                2
        );
    }

    @Test
    @DisplayName("존재하는 영화관을 삭제하면 DB에서 제거된다")
    @Transactional
    void deleteTheater_success() {
        Theater theater = createAndSaveTheater("강남점");

        assertThat(theaterJpaPort.findById(theater.getId())).isPresent();

        pipeline.send(new DeleteTheaterCommand(theater.getId()));
        entityManager.flush();
        entityManager.clear();

        assertThat(theaterJpaPort.findById(theater.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 영화관 ID로 삭제를 요청하면 실패한다")
    @Transactional
    void deleteTheater_withNonExistentId_fails() {
        assertThatThrownBy(() -> pipeline.send(new DeleteTheaterCommand(999999L)))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("영화관 데이터가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("연결된 상영관이 있는 영화관을 삭제하면 실패한다")
    @Transactional
    void deleteTheater_withLinkedAuditorium_fails() {
        Theater theater = createAndSaveTheater("홍대점");
        insertAuditorium(theater.getId(), "1관");

        assertThatThrownBy(() -> pipeline.send(new DeleteTheaterCommand(theater.getId())))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");

        assertThat(theaterJpaPort.findById(theater.getId())).isPresent();
    }
}
