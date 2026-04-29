package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.condition.MovieScreeningPresence;
import com.movie.shop.api.movie.domain.condition.MovieTitleUniquenessCondition;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "movie")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter(AccessLevel.PRIVATE)
    @AttributeOverride(
        name = "title",
        column = @Column(
            name = "title",
            nullable = false,
            length = 200,
            unique = true
        )
    )
    @Embedded
    private MovieTitle title;

    @NotBlank(message = "감독 이름은 필수입니다.")
    @Size(max = 100, message = "감독 이름은 100자를 초과할 수 없습니다.")
    @Column(nullable = false, length = 100)
    private String director;

    @NotEmpty(message = "최소 하나 이상의 장르가 필요합니다.")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "genres_json", columnDefinition = "json", nullable = false)
    private List<
        @NotBlank(
            message = "장르는 빈 값이나 공백을 포함할 수 없습니다."
        ) String
    > genres = new ArrayList<>();

    @Positive(message = "상영 시간은 0보다 커야 합니다.")
    @Column(nullable = false)
    private int runtimeMinutes;

    @NotNull(message = "유효하지 않은 관람 등급입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudienceRating audienceRating;

    @NotBlank(message = "시놉시스는 필수입니다.")
    @Size(max = 1000, message = "시놉시스는 1000자를 초과할 수 없습니다.")
    @Column(nullable = false, length = 1000)
    private String synopsis;

    @NotNull(message = "개봉일은 필수입니다.")
    @Column(nullable = false)
    private OffsetDateTime releaseDate;

    @NotEmpty(message = "최소 한 명 이상의 출연진이 필요합니다.")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "casts_json", columnDefinition = "json", nullable = false)
    private List<Actor> casts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    public static Movie register(
        MovieTitleUniquenessCondition titleCondition,
        String title,
        String director,
        List<String> genres,
        int runtimeMinutes,
        AudienceRating audienceRating,
        String synopsis,
        OffsetDateTime releaseDate,
        List<Actor> casts
    ) {
        var movie = new Movie();
        movie.director = director;
        movie.genres = genres;
        movie.runtimeMinutes = runtimeMinutes;
        movie.audienceRating = audienceRating;
        movie.synopsis = synopsis;
        movie.releaseDate = releaseDate;
        movie.casts = casts;
        movie.status = MovieStatus.PREPARING;
        movie.title = MovieTitle.createNew(title, titleCondition);

        EntityValidator.create()
            .validateBean(movie)
            .throwIfInvalid(MovieDomainException::new);

        return movie;
    }

    public void update(
        MovieTitleUniquenessCondition titleCondition,
        String title,
        String director,
        List<String> genres,
        int runtimeMinutes,
        AudienceRating audienceRating,
        String synopsis,
        OffsetDateTime releaseDate,
        List<Actor> casts
    ) {
        this.director = director;
        this.genres = genres;
        this.runtimeMinutes = runtimeMinutes;
        this.audienceRating = audienceRating;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.casts = casts;
        this.title = MovieTitle.createFrom(
            this.title,
            title,
            titleCondition
        );

        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(MovieDomainException::new);
    }

    public void validateCanDelete(MovieScreeningPresence screeningPresence) {
        validateCanRemove();

        if (id == null) {
            throw new MovieDomainException("영화 ID가 존재하지 않습니다.");
        }

        if (screeningPresence == null) {
            throw new MovieDomainException("영화의 상영 연결 여부는 필수입니다.");
        }

        if (screeningPresence.hasAnyScreening()) {
            throw new MovieDomainException("상영이 연결된 영화는 삭제할 수 없습니다.");
        }
    }

    private void validateCanRemove() {
        if (status == MovieStatus.NOW_SHOWING) {
            throw new MovieDomainException(
                "NOW_SHOWING 상태의 영화는 삭제할 수 없습니다."
            );
        }
    }

    public boolean canBeScheduled() {
        return (
            status == MovieStatus.COMING_SOON ||
            status == MovieStatus.NOW_SHOWING
        );
    }

    public void changeState(MovieStateChange stateChange) {
        if (stateChange == null) {
            throw new MovieDomainException("변경할 영화 상태는 필수입니다.");
        }

        status = status.transition(stateChange);
    }
}
