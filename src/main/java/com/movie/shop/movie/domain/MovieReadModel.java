/**
 * MovieReadModel.java
 * 
 * @description 영화 정보 조회를 위한 읽기 전용 도메인 모델 클래스.
 *              CQRS 패턴의 Query 모델로서 영화 상세 정보를 담는 불변 엔티티이다.
 * 
 * @author      movie-shop Development Team
 * @version     1.0.0
 * @since       2025-12-02
 * 
 * @modification
 * <pre>
 * 수정일        수정자       수정내용
 * ----------  --------    ---------------------------
 * 2025-12-02   정수민       최초 생성
 * </pre>
 */
package com.movie.shop.movie.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Entity
@Table(name = "MOVIE_READ_MODEL")
@Immutable // 읽기 전용 모델이므로 수정 방지
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieReadModel {

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    private String title;

    private String director;

    @Column(columnDefinition = "TEXT") // DB의 TEXT 타입 매핑
    private String synopsis;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "running_time_minutes")
    private Integer runningTimeMinutes;

    @Column(name = "audience_rating")
    private String audienceRating; // 'ALL', 'PG12' 등

    private String status; // 'NOW_SHOWING' 등

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "genres_as_string", columnDefinition = "TEXT")
    private String genresAsString; // "액션,스릴러" 형태

    @Column(name = "cast_as_json", columnDefinition = "JSON")
    private String castAsJson; // JSON 문자열로 그대로 매핑

    @Column(name = "still_cut_urls", columnDefinition = "TEXT")
    private String stillCutUrls; // 콤마로 구분된 URL 목록

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "booking_rate")
    private Double bookingRate;

    @Column(name = "total_audience_count")
    private Integer totalAudienceCount;
}