package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.policy.MovieTitleDuplicateValidator;
import com.movie.shop.api.movie.domain.policy.status.MovieTitleDuplication;
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
@DisplayName("UpdateMovieCommandHandler 통합 테스트")
class UpdateMovieCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieJpaPort movieJpaPort;

    @Autowired
    private EntityManager entityManager;

    private MovieTitleDuplicateValidator nonDuplicateTitleValidator() {
        return new MovieTitleDuplicateValidator(new MovieTitleDuplication(false));
    }

    private Movie createAndSaveMovie(String title) {
        Actor actor = new Actor(
                "매튜 매코너히",
                OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                "USA",
                "쿠퍼"
        );

        Movie movie = Movie.Register(
                nonDuplicateTitleValidator(),
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
    @DisplayName("영화 수정 성공 테스트")
    class UpdateMovieSuccessTest {

        @Test
        @Transactional
        @DisplayName("영화 정보 전체 수정하면 DB에 변경사항이 반영된다")
        void updateMovie_allFields_updatesDatabase() {
            // Given: DB에 영화 저장
            Movie movie = createAndSaveMovie("인터스텔라");
            Long movieId = movie.getId();

            // When: 영화 정보 수정
            UpdateMovieCommand command = new UpdateMovieCommand(
                    movieId,
                    "인터스텔라 디렉터스컷",
                    "크리스토퍼 놀란 감독",
                    List.of("SF", "드라마", "모험"),
                    180,
                    AudienceRating.PG15,
                    "우주 탐사를 통해 인류의 미래를 찾는 감동적인 이야기",
                    OffsetDateTime.parse("2024-11-07T00:00:00Z"),
                    List.of(
                            new UpdateMovieCommand.UpdateActorDto(
                                    "매튜 매코너히",
                                    OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                                    "USA",
                                    "쿠퍼"
                            ),
                            new UpdateMovieCommand.UpdateActorDto(
                                    "앤 해서웨이",
                                    OffsetDateTime.parse("1982-11-12T00:00:00Z"),
                                    "USA",
                                    "아멜리아 브랜드"
                            )
                    )
            );

            Long resultId = pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: DB에서 영화 정보가 변경되었는지 확인
            Movie updatedMovie = movieJpaPort.findById(movieId).orElseThrow();

            assertThat(resultId).isEqualTo(movieId);
            assertThat(updatedMovie.getTitle().getTitle()).isEqualTo("인터스텔라 디렉터스컷");
            assertThat(updatedMovie.getDirector()).isEqualTo("크리스토퍼 놀란 감독");
            assertThat(updatedMovie.getGenres()).containsExactlyInAnyOrder("SF", "드라마", "모험");
            assertThat(updatedMovie.getRuntimeMinutes()).isEqualTo(180);
            assertThat(updatedMovie.getAudienceRating()).isEqualTo(AudienceRating.PG15);
            assertThat(updatedMovie.getSynopsis()).isEqualTo("우주 탐사를 통해 인류의 미래를 찾는 감동적인 이야기");
            assertThat(updatedMovie.getReleaseDate()).isEqualTo(OffsetDateTime.parse("2024-11-07T00:00:00Z"));
            assertThat(updatedMovie.getCasts()).hasSize(2);
        }

        @Test
        @Transactional
        @DisplayName("출연진 정보만 수정하면 DB에 변경사항이 반영된다")
        void updateMovie_onlyCasts_updatesDatabase() {
            // Given: DB에 영화 저장
            Movie movie = createAndSaveMovie("다크나이트");
            Long movieId = movie.getId();

            // When: 출연진만 변경
            UpdateMovieCommand command = new UpdateMovieCommand(
                    movieId,
                    "다크나이트",
                    "크리스토퍼 놀란",
                    List.of("SF", "드라마"),
                    169,
                    AudienceRating.PG12,
                    "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                    OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                    List.of(
                            new UpdateMovieCommand.UpdateActorDto(
                                    "크리스찬 베일",
                                    OffsetDateTime.parse("1974-01-30T00:00:00Z"),
                                    "UK",
                                    "브루스 웨인"
                            ),
                            new UpdateMovieCommand.UpdateActorDto(
                                    "히스 레저",
                                    OffsetDateTime.parse("1979-04-04T00:00:00Z"),
                                    "Australia",
                                    "조커"
                            )
                    )
            );

            pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: 출연진이 변경되었는지 확인
            Movie updatedMovie = movieJpaPort.findById(movieId).orElseThrow();

            assertThat(updatedMovie.getCasts()).hasSize(2);
            assertThat(updatedMovie.getCasts())
                    .extracting(Actor::getName)
                    .containsExactlyInAnyOrder("크리스찬 베일", "히스 레저");
        }
    }

    @Nested
    @DisplayName("영화 제목 수정 테스트")
    class UpdateMovieTitleTest {

        @Test
        @Transactional
        @DisplayName("새로운 제목으로 변경하면 성공한다")
        void updateMovie_withNewTitle_success() {
            // Given: DB에 영화 저장
            Movie movie = createAndSaveMovie("오펜하이머");
            Long movieId = movie.getId();

            // When: 제목만 변경
            UpdateMovieCommand command = new UpdateMovieCommand(
                    movieId,
                    "오펜하이머 IMAX",
                    "크리스토퍼 놀란",
                    List.of("SF", "드라마"),
                    169,
                    AudienceRating.PG12,
                    "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                    OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                    List.of(
                            new UpdateMovieCommand.UpdateActorDto(
                                    "매튜 매코너히",
                                    OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                                    "USA",
                                    "쿠퍼"
                            )
                    )
            );

            pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: 제목이 변경되었는지 확인
            Movie updatedMovie = movieJpaPort.findById(movieId).orElseThrow();
            assertThat(updatedMovie.getTitle().getTitle()).isEqualTo("오펜하이머 IMAX");
        }

        @Test
        @Transactional
        @DisplayName("동일한 제목으로 변경하면 중복 검증을 스킵하고 성공한다")
        void updateMovie_withSameTitle_successWithoutDuplicateCheck() {
            // Given: DB에 영화 저장
            Movie movie = createAndSaveMovie("테넷");
            Long movieId = movie.getId();
            String originalTitle = movie.getTitle().getTitle();

            // When: 동일한 제목으로 수정 요청 (다른 필드만 변경)
            UpdateMovieCommand command = new UpdateMovieCommand(
                    movieId,
                    "테넷",  // 동일한 제목
                    "놀란 감독",  // 감독 이름 변경
                    List.of("SF", "액션"),
                    150,
                    AudienceRating.PG15,
                    "시간 역행을 다룬 이야기",
                    OffsetDateTime.parse("2020-08-26T00:00:00Z"),
                    List.of(
                            new UpdateMovieCommand.UpdateActorDto(
                                    "존 데이비드 워싱턴",
                                    OffsetDateTime.parse("1984-07-28T00:00:00Z"),
                                    "USA",
                                    "주인공"
                            )
                    )
            );

            pipeline.send(command);

            entityManager.flush();
            entityManager.clear();

            // Then: 제목은 그대로, 다른 필드가 변경되었는지 확인
            Movie updatedMovie = movieJpaPort.findById(movieId).orElseThrow();
            assertThat(updatedMovie.getTitle().getTitle()).isEqualTo(originalTitle);
            assertThat(updatedMovie.getDirector()).isEqualTo("놀란 감독");
            assertThat(updatedMovie.getRuntimeMinutes()).isEqualTo(150);
        }

        @Test
        @Transactional
        @DisplayName("이미 존재하는 다른 영화의 제목으로 변경하면 실패한다")
        void updateMovie_withDuplicateTitle_throwsException() {
            // Given: DB에 두 개의 영화 저장
            createAndSaveMovie("덩케르크");
            Movie movie2 = createAndSaveMovie("메멘토");
            Long movie2Id = movie2.getId();

            // When & Then: 두 번째 영화의 제목을 첫 번째 영화 제목으로 변경 시도
            UpdateMovieCommand command = new UpdateMovieCommand(
                    movie2Id,
                    "덩케르크",  // 이미 존재하는 제목
                    "크리스토퍼 놀란",
                    List.of("SF", "드라마"),
                    169,
                    AudienceRating.PG12,
                    "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                    OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                    List.of(
                            new UpdateMovieCommand.UpdateActorDto(
                                    "매튜 매코너히",
                                    OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                                    "USA",
                                    "쿠퍼"
                            )
                    )
            );

            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(MovieDomainException.class)
                    .hasMessageContaining("덩케르크");
        }

    }

    @Nested
    @DisplayName("영화 수정 실패 테스트")
    class UpdateMovieFailureTest {

        @Test
        @Transactional
        @DisplayName("존재하지 않는 영화 ID로 수정하면 예외가 발생한다")
        void updateMovie_withNonExistentId_throwsException() {
            // Given: 존재하지 않는 영화 ID
            long nonExistentMovieId = 999999L;

            // When & Then: 존재하지 않는 영화 수정 시도 시 예외 발생
            UpdateMovieCommand command = new UpdateMovieCommand(
                    nonExistentMovieId,
                    "존재하지 않는 영화",
                    "크리스토퍼 놀란",
                    List.of("SF", "드라마"),
                    169,
                    AudienceRating.PG12,
                    "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                    OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                    List.of(
                            new UpdateMovieCommand.UpdateActorDto(
                                    "매튜 매코너히",
                                    OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                                    "USA",
                                    "쿠퍼"
                            )
                    )
            );

            assertThatThrownBy(() -> pipeline.send(command))
                    .isInstanceOf(MovieDomainException.class);
        }
    }
}
