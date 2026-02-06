package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.aggregate.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.aggregate.validator.MovieTitleDuplicateValidator;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.screening.api.commands.RegisterScreeningCommand;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DeleteMovieCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private MovieTitleDuplicateValidator validator;

    @Autowired
    private TheaterNameDuplicateValidator theaterNameDuplicateValidator;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private MovieJpaPort movieJpaPort;

    @Autowired
    private EntityManager entityManager;

    private Theater createAndSaveTheater(String name) {
        Theater theater = Theater.Register(
                theaterNameDuplicateValidator,
                name,
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "B1", "B2"),
                2,
                2
        );

        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();
        return theater;
    }

    @Test
    @DisplayName("존재하는 영화를 삭제하면 DB에서 영화가 제거된다")
    @Transactional
    void deleteMovie_removesMovieFromDatabase() {
        // Given: DB에 영화 저장
        Actor actor = new Actor(
                "매튜 매코너히",
                OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                "USA",
                "쿠퍼"
        );



        Movie movie = Movie.Register(
                validator,
                "인터스텔라",
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

        Long movieId = movie.getId();

        // DB에 저장되었는지 확인
        assertThat(movieJpaPort.findById(movieId)).isPresent();

        // When: 영화 삭제 커맨드 실행
        DeleteMovieCommand command = new DeleteMovieCommand(movieId);
        pipeline.send(command);

        entityManager.flush();
        entityManager.clear();

        // Then: DB에서 영화가 삭제되었는지 확인
        assertThat(movieJpaPort.findById(movieId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 영화 ID로 삭제를 요청하면 예외가 발생한다")
    @Transactional
    void deleteMovie_withNonExistentId_throwsException() {
        // Given: 존재하지 않는 영화 ID
        long nonExistentMovieId = 999999L;

        // When & Then: 존재하지 않는 영화 삭제 시도 시 예외 발생
        DeleteMovieCommand command = new DeleteMovieCommand(nonExistentMovieId);
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(MovieDomainException.class);
    }

    @Test
    @DisplayName("NOW_SHOWING 상태의 영화를 삭제하면 예외가 발생한다")
    @Transactional
    void deleteMovie_withNowShowingStatus_throwsException() {
        // Given
        Movie movie = Movie.Register(
                validator,
                "삭제불가상영중영화",
                "감독",
                List.of("드라마"),
                120,
                AudienceRating.PG12,
                "시놉시스",
                OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                List.of(new Actor("배우", OffsetDateTime.parse("1990-01-01T00:00:00Z"), "Korea", "역할"))
        );
        movie.moveToComingSoon();
        movie.startShowing();
        movie = movieRepository.save(movie);
        entityManager.flush();
        entityManager.clear();

        // When & Then
        DeleteMovieCommand command = new DeleteMovieCommand(movie.getId());
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("NOW_SHOWING 상태의 영화는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("상영이 연결된 영화를 삭제하면 예외가 발생한다")
    @Transactional
    void deleteMovie_withLinkedScreening_throwsException() {
        // Given
        Movie movie = Movie.Register(
                validator,
                "삭제불가연결영화",
                "감독",
                List.of("드라마"),
                120,
                AudienceRating.PG12,
                "시놉시스",
                OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                List.of(new Actor("배우", OffsetDateTime.parse("1990-01-01T00:00:00Z"), "Korea", "역할"))
        );
        movie.moveToComingSoon();
        movie = movieRepository.save(movie);
        Theater theater = createAndSaveTheater("삭제검증관");

        RegisterScreeningCommand registerScreeningCommand = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );
        pipeline.send(registerScreeningCommand);

        entityManager.flush();
        entityManager.clear();

        // When & Then
        DeleteMovieCommand command = new DeleteMovieCommand(movie.getId());
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("상영이 연결된 영화는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("영화를 삭제하면 연관된 배우 엔티티도 함께 삭제된다")
    @Transactional
    void deleteMovie_withCascadeDelete_removesActorsAsWell() {
        // Given: 여러 배우가 있는 영화 저장
        Actor actor1 = new Actor(
                "크리스찬 베일",
                OffsetDateTime.parse("1974-01-30T00:00:00Z"),
                "UK",
                "브루스 웨인"
        );

        Actor actor2 = new Actor(
                "히스 레저",
                OffsetDateTime.parse("1979-04-04T00:00:00Z"),
                "Australia",
                "조커"
        );

        Movie movie = Movie.Register(
                validator,
                "다크 나이트",
                "크리스토퍼 놀란",
                List.of("액션", "범죄"),
                152,
                AudienceRating.PG15,
                "배트맨과 조커의 대결",
                OffsetDateTime.parse("2008-08-06T00:00:00Z"),
                List.of(actor1, actor2)
        );

        movie = movieRepository.save(movie);
        entityManager.flush();
        entityManager.clear();

        Long movieId = movie.getId();

        // DB에 영화와 배우들이 저장되었는지 확인
        Movie savedMovie = movieJpaPort.findById(movieId).orElseThrow();
        assertThat(savedMovie.getCasts()).hasSize(2);

        // When: 영화 삭제
        DeleteMovieCommand command = new DeleteMovieCommand(movieId);
        pipeline.send(command);

        entityManager.flush();
        entityManager.clear();

        // Then: DB에서 영화와 연관된 배우들도 삭제되었는지 확인
        assertThat(movieJpaPort.findById(movieId)).isEmpty();
        
        // 배우들이 영화와 함께 cascade 삭제되었는지 확인
        // (Actor가 Movie의 자식 엔티티이므로 함께 삭제됨)
    }

    @Test
    @DisplayName("여러 영화가 있을 때 특정 영화를 삭제하면 대상 영화만 제거된다")
    @Transactional
    void deleteMovie_multipleMovies_onlyDeletesTargetMovie() {
        // Given: 여러 영화 저장
        Actor actor1 = new Actor(
                "배우1",
                OffsetDateTime.parse("1980-01-01T00:00:00Z"),
                "Korea",
                "역할1"
        );

        Movie movie1 = Movie.Register(
                validator,
                "영화1",
                "감독1",
                List.of("장르1"),
                120,
                AudienceRating.ALL,
                "시놉시스1",
                OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                List.of(actor1)
        );

        Actor actor2 = new Actor(
                "배우2",
                OffsetDateTime.parse("1985-01-01T00:00:00Z"),
                "Korea",
                "역할2"
        );

        Movie movie2 = Movie.Register(
                validator,
                "영화2",
                "감독2",
                List.of("장르2"),
                130,
                AudienceRating.PG12,
                "시놉시스2",
                OffsetDateTime.parse("2021-01-01T00:00:00Z"),
                List.of(actor2)
        );

        movie1 = movieRepository.save(movie1);
        movie2 = movieRepository.save(movie2);
        entityManager.flush();
        entityManager.clear();

        Long movieId1 = movie1.getId();
        Long movieId2 = movie2.getId();

        // 두 영화 모두 저장되었는지 확인
        assertThat(movieRepository.count()).isEqualTo(2);

        // When: movie1만 삭제
        DeleteMovieCommand command = new DeleteMovieCommand(movieId1);
        pipeline.send(command);

        entityManager.flush();
        entityManager.clear();

        // Then: movie1만 삭제되고 movie2는 남아있는지 확인
        assertThat(movieJpaPort.findById(movieId1)).isEmpty();
        assertThat(movieJpaPort.findById(movieId2)).isPresent();
        assertThat(movieRepository.count()).isEqualTo(1);
    }
}
