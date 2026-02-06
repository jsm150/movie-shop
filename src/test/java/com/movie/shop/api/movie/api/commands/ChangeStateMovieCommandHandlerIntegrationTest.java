package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.aggregate.MovieStateChange;
import com.movie.shop.api.movie.domain.aggregate.MovieStatus;
import com.movie.shop.api.movie.domain.aggregate.validator.MovieTitleDuplicateValidator;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("ChangeStateMovieCommandHandler 통합 테스트")
class ChangeStateMovieCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private MovieTitleDuplicateValidator validator;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private EntityManager entityManager;

    private Movie createAndSaveMovie(String title) {
        Actor actor = new Actor(
                "매튜 매코너히",
                OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                "USA",
                "쿠퍼"
        );

        Movie movie = Movie.Register(
                validator,
                title,
                "크리스토퍼 놀란",
                List.of("SF", "드라마"),
                169,
                AudienceRating.PG12,
                "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                List.of(actor)
        );

        movie = movieRepository.save(movie);
        entityManager.flush();
        entityManager.clear();

        return movie;
    }

    @Nested
    @DisplayName("PREPARING -> COMING_SOON 상태 변경 테스트")
    class MoveToComingSoonTest {

        @Test
        @Transactional
        @DisplayName("PREPARING 상태의 영화를 COMING_SOON으로 변경한다")
        void changeState_preparingToComingSoon_success() {
            // Given: PREPARING 상태의 영화
            Movie movie = createAndSaveMovie("인터스텔라");
            Long movieId = movie.getId();
            assertThat(movie.getStatus()).isEqualTo(MovieStatus.PREPARING);

            // When: COMING_SOON으로 상태 변경
            ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                    movieId,
                    MovieStateChange.COMING_SOON
            );
            pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: 상태가 COMING_SOON으로 변경됨
            Movie updatedMovie = movieRepository.getById(movieId);
            assertThat(updatedMovie.getStatus()).isEqualTo(MovieStatus.COMING_SOON);
        }

        @Test
        @Transactional
        @DisplayName("PREPARING이 아닌 상태에서 COMING_SOON으로 변경하면 예외가 발생한다")
        void changeState_notPreparingToComingSoon_throwsException() {
            // Given: PREPARING 상태의 영화를 먼저 COMING_SOON으로 변경
            Movie movie = createAndSaveMovie("인터스텔라");
            Long movieId = movie.getId();
            movie.moveToComingSoon();
            movieRepository.save(movie);
            entityManager.flush();
            entityManager.clear();

            // When & Then: COMING_SOON 상태에서 다시 COMING_SOON으로 변경 시도 시 예외 발생
            ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                    movieId,
                    MovieStateChange.COMING_SOON
            );
            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(MovieDomainException.class);
        }
    }

    @Nested
    @DisplayName("COMING_SOON -> NOW_SHOWING 상태 변경 테스트")
    class StartShowingTest {

        @Test
        @Transactional
        @DisplayName("COMING_SOON 상태의 영화를 NOW_SHOWING으로 변경한다")
        void changeState_comingSoonToNowShowing_success() {
            // Given: COMING_SOON 상태의 영화
            Movie movie = createAndSaveMovie("인터스텔라");
            movie.moveToComingSoon();
            movieRepository.save(movie);
            entityManager.flush();
            entityManager.clear();

            Long movieId = movie.getId();

            // When: NOW_SHOWING으로 상태 변경
            ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                    movieId,
                    MovieStateChange.NOW_SHOWING
            );
            pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: 상태가 NOW_SHOWING으로 변경됨
            Movie updatedMovie = movieRepository.getById(movieId);
            assertThat(updatedMovie.getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);
        }

        @Test
        @Transactional
        @DisplayName("COMING_SOON이 아닌 상태에서 NOW_SHOWING으로 변경하면 예외가 발생한다")
        void changeState_notComingSoonToNowShowing_throwsException() {
            // Given: PREPARING 상태의 영화
            Movie movie = createAndSaveMovie("인터스텔라");
            Long movieId = movie.getId();

            // When & Then: PREPARING 상태에서 NOW_SHOWING으로 변경 시도 시 예외 발생
            ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                    movieId,
                    MovieStateChange.NOW_SHOWING
            );
            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(MovieDomainException.class);
        }
    }

    @Nested
    @DisplayName("NOW_SHOWING -> ENDED 상태 변경 테스트")
    class EndShowingTest {

        @Test
        @Transactional
        @DisplayName("NOW_SHOWING 상태의 영화를 ENDED로 변경한다")
        void changeState_nowShowingToEnded_success() {
            // Given: NOW_SHOWING 상태의 영화
            Movie movie = createAndSaveMovie("인터스텔라");
            movie.moveToComingSoon();
            movie.startShowing();
            movieRepository.save(movie);
            entityManager.flush();
            entityManager.clear();

            Long movieId = movie.getId();

            // When: ENDED로 상태 변경
            ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                    movieId,
                    MovieStateChange.ENDED
            );
            pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: 상태가 ENDED로 변경됨
            Movie updatedMovie = movieRepository.getById(movieId);
            assertThat(updatedMovie.getStatus()).isEqualTo(MovieStatus.ENDED);
        }

        @Test
        @Transactional
        @DisplayName("NOW_SHOWING이 아닌 상태에서 ENDED로 변경하면 예외가 발생한다")
        void changeState_notNowShowingToEnded_throwsException() {
            // Given: COMING_SOON 상태의 영화
            Movie movie = createAndSaveMovie("인터스텔라");
            movie.moveToComingSoon();
            movieRepository.save(movie);
            entityManager.flush();
            entityManager.clear();

            Long movieId = movie.getId();

            // When & Then: COMING_SOON 상태에서 ENDED로 변경 시도 시 예외 발생
            ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                    movieId,
                    MovieStateChange.ENDED
            );
            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(MovieDomainException.class);
        }
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 영화 ID로 상태 변경하면 예외가 발생한다")
    void changeState_withNonExistentId_throwsException() {
        // Given: 존재하지 않는 영화 ID
        long nonExistentMovieId = 999999L;

        // When & Then: 존재하지 않는 영화의 상태 변경 시도 시 예외 발생
        ChangeStateMovieCommand command = new ChangeStateMovieCommand(
                nonExistentMovieId,
                MovieStateChange.COMING_SOON
        );
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(MovieDomainException.class);
    }
}
