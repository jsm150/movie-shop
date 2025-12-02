/**
 * MovieSummaryData.java
 * 
 * @description 영화 요약 정보를 담는 DTO(Data Transfer Object) 클래스.
 *              영화 목록 화면에서 필요한 핵심 정보만을 전달하기 위해 사용된다.
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
 * 
 * @see com.movie.shop.movie.domain.MovieReadModel
 */
package com.movie.shop.movie.service;

import com.movie.shop.movie.domain.MovieReadModel;
import lombok.Getter;

@Getter
public class MovieSummaryData {
    private Long movieId;
    private String title;
    private String posterImageUrl;
    private String audienceRating;
    private Double bookingRate;
    private Double averageRating;

    public MovieSummaryData(MovieReadModel entity) {
        this.movieId = entity.getMovieId();
        this.title = entity.getTitle();
        this.posterImageUrl = entity.getPosterImageUrl();
        this.audienceRating = entity.getAudienceRating();
        this.bookingRate = entity.getBookingRate();
        this.averageRating = entity.getAverageRating();
    }
}