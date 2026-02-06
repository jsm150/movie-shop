package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.aggregate.validator.MovieTitleDuplicateValidator;
import com.movie.shop.api.movie.domain.aggregate.newtype.MovieTitle;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
            column = @Column(name = "title", nullable = false, length = 200, unique = true)
    )
    @Embedded
    private MovieTitle title;
    
    @NotBlank(message = "감독 이름은 필수입니다.")
    @Size(max = 100, message = "감독 이름은 100자를 초과할 수 없습니다.")
    @Column(nullable = false, length = 100)
    private String director;
    
    @NotEmpty(message = "최소 하나 이상의 장르가 필요합니다.")
    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre", nullable = false)
    private List<@NotBlank(message = "장르는 빈 값이나 공백을 포함할 수 없습니다.") String> genres = new ArrayList<>();
    
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
    @ElementCollection
    @CollectionTable(name = "movie_actors", joinColumns = @JoinColumn(name = "movie_id"))
    private List<Actor> casts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    public static Movie Register(MovieTitleDuplicateValidator titleDuplicateValidator, String title, String director, List<String> genres, int runtimeMinutes,
                                 AudienceRating audienceRating, String synopsis, OffsetDateTime releaseDate, List<Actor> casts) {
        
        var movie = new Movie();
        movie.director = director;
        movie.genres = genres;
        movie.runtimeMinutes = runtimeMinutes;
        movie.audienceRating = audienceRating;
        movie.synopsis = synopsis;
        movie.releaseDate = releaseDate;
        movie.casts = casts;
        movie.status = MovieStatus.PREPARING;

        EntityValidator.create()
            .apply(MovieTitle.createNew(title, titleDuplicateValidator), movie::setTitle)
            .validateBean(movie)
            .throwIfInvalid(MovieDomainException::new);

        return movie;
    }

    public void Update(MovieTitleDuplicateValidator titleDuplicateValidator, String title, String director, List<String> genres,
                       int runtimeMinutes, AudienceRating audienceRating, String synopsis,
                       OffsetDateTime releaseDate, List<Actor> casts) {
        
        this.director = director;
        this.genres = genres;
        this.runtimeMinutes = runtimeMinutes;
        this.audienceRating = audienceRating;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.casts = casts;

        EntityValidator.create()
            .apply(MovieTitle.createFrom(this.title, title, titleDuplicateValidator), this::setTitle)
            .validateBean(this)
            .throwIfInvalid(MovieDomainException::new);
    }

    public boolean canRemove() {
        return status != MovieStatus.NOW_SHOWING;
    }

    public boolean canBeScheduled() {
        return status == MovieStatus.COMING_SOON || status == MovieStatus.NOW_SHOWING;
    }

    public void moveToComingSoon()
    {
        if (status == MovieStatus.PREPARING)
        {
            status = MovieStatus.COMING_SOON;
        }
        else
        {
            throw new MovieDomainException("PREPARING 이 아닌 상태에서 COMING_SOON으로 변경하려고 함.");
        }
    }

    public void startShowing()
    {
        if (status == MovieStatus.COMING_SOON)
        {
            status = MovieStatus.NOW_SHOWING;
        }
        else
        {
            throw new MovieDomainException("COMING_SOON 이 아닌 상태에서 NOW_SHOWING으로 변경하려고 함.");
        }
    }

    public void endShowing()
    {
        if (status == MovieStatus.NOW_SHOWING)
        {
            status = MovieStatus.ENDED;
        }
        else
        {
            throw new MovieDomainException("NOW_SHOWING 이 아닌 상태에서 ENDED로 변경하려고 함.");
        }
    }
}
