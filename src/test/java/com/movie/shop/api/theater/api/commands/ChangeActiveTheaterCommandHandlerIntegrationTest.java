package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
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
@DisplayName("ChangeActiveTheaterCommandHandler 통합 테스트")
class ChangeActiveTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterNameDuplicateValidator validator;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private EntityManager entityManager;

    private Theater createAndSaveTheater(String name, boolean active) {
        Theater theater = Theater.Register(
                validator,
                name,
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                2,
                3
        );

        if (!active) {
            theater.deactivate();
        }

        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();

        return theater;
    }

    @Test
    @Transactional
    @DisplayName("활성화된 극장을 비활성화한다")
    void changeActive_deactivate_success() {
        // given
        Theater theater = createAndSaveTheater("1관", true);
        long theaterId = theater.getId();

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                theaterId,
                TheaterActiveChange.DEACTIVATE
        );

        // when
        pipeline.send(command);
        entityManager.flush();
        entityManager.clear();

        // then
        Theater updated = theaterRepository.getById(theaterId);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("비활성화된 극장을 활성화한다")
    void changeActive_activate_success() {
        // given
        Theater theater = createAndSaveTheater("2관", false);
        long theaterId = theater.getId();

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                theaterId,
                TheaterActiveChange.ACTIVATE
        );

        // when
        pipeline.send(command);
        entityManager.flush();
        entityManager.clear();

        // then
        Theater updated = theaterRepository.getById(theaterId);
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 극장 ID로 상태 변경 시 예외가 발생한다")
    void changeActive_withNonExistentId_throwsException() {
        // given
        long nonExistentTheaterId = 999999L;

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                nonExistentTheaterId,
                TheaterActiveChange.DEACTIVATE
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
    }
}
