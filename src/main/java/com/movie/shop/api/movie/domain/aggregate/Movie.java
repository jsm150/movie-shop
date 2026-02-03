package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.shared.domain.DomainValidator;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(nullable = false, length = 200, unique = true)
    private String title;
    
    @Column(nullable = false, length = 100)
    private String director;
    
    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre", nullable = false)
    private List<String> genres = new ArrayList<>();
    
    @Column(nullable = false)
    private int runtimeMinutes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AudienceRating audienceRating;
    
    @Column(nullable = false, length = 1000)
    private String synopsis;
    
    @Column(nullable = false)
    private OffsetDateTime releaseDate;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movie_id")
    private List<Actor> casts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    public static Movie Register(MovieRepository repository, String title, String director, List<String> genres, int runtimeMinutes,
                 AudienceRating audienceRating, String synopsis, OffsetDateTime releaseDate, List<Actor> casts) {
        
        DomainValidator.builder()
            // 제목 검증
            .notBlank(title, "영화 제목은 필수입니다.")
            .maxLength(title, 200, "영화 제목은 200자를 초과할 수 없습니다.")
            
            // 감독 검증
            .notBlank(director, "감독 이름은 필수입니다.")
            .maxLength(director, 100, "감독 이름은 100자를 초과할 수 없습니다.")
            
            // 장르 검증
            .validate(genres != null && !genres.isEmpty(), "최소 하나 이상의 장르가 필요합니다.")
            .validate(() -> genres == null || genres.stream().noneMatch(g -> g == null || g.isBlank()), 
                     "장르는 빈 값이나 공백을 포함할 수 없습니다.")
            
            // 상영 시간 검증
            .positive(runtimeMinutes, "상영 시간은 0보다 커야 합니다.")
            
            // 관람 등급 검증
            .notNull(audienceRating, "유효하지 않은 관람 등급입니다.")
            
            // 시놉시스 검증
            .notBlank(synopsis, "시놉시스는 필수입니다.")
            .maxLength(synopsis, 1000, "시놉시스는 1000자를 초과할 수 없습니다.")
            
            // 개봉일 검증
            .notNull(releaseDate, "개봉일은 필수입니다.")
            
            // 출연진 검증
            .validate(casts != null && !casts.isEmpty(), "최소 한 명 이상의 출연진이 필요합니다.")
            
            // 중복 제목 검증
            .validate(() -> title == null || !repository.existsByTitle(title), 
                     "'" + title + "' 제목을 가진 영화가 이미 존재합니다.")
            
            .throwIfInvalid(MovieDomainException::new);

        var movie = new Movie();

        movie.title = title;
        movie.director = director;
        movie.genres = new ArrayList<>(genres);
        movie.runtimeMinutes = runtimeMinutes;
        movie.audienceRating = audienceRating;
        movie.synopsis = synopsis;
        movie.releaseDate = releaseDate;
        movie.casts = new ArrayList<>(casts);
        movie.status = MovieStatus.PREPARING;

        return movie;
    }
}