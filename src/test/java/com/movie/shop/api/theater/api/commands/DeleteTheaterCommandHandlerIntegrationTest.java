package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DeleteTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterNameDuplicateValidator validator;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void deleteTheater_removesTheaterFromDatabase() {
        // Given
        Theater theater = Theater.Register(
                validator,
                "1관",
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                2,
                3
        );

        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();

        long theaterId = theater.getId();

        assertThat(theaterRepository.findById(theaterId)).isPresent();

        // When
        DeleteTheaterCommand command = new DeleteTheaterCommand(theaterId);
        pipeline.send(command);

        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(theaterRepository.findById(theaterId)).isEmpty();
    }

    @Test
    @Transactional
    void deleteTheater_withNonExistentId_throwsException() {
        // Given
        long nonExistentTheaterId = 999999L;

        // When & Then
        DeleteTheaterCommand command = new DeleteTheaterCommand(nonExistentTheaterId);
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
    }
}
