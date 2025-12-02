/**
 * MovieQueryService.java
 * 
 * @description 영화 조회 관련 비즈니스 로직을 처리하는 서비스 클래스.
 *              현재 상영 중인 영화 목록을 다양한 정렬 기준으로 조회하는 기능을 제공한다.
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
 * @see com.movie.shop.movie.repository.MovieReadModelRepository
 * @see com.movie.shop.movie.service.MovieSummaryData
 */
package com.movie.shop.movie.service;

import com.movie.shop.movie.domain.MovieReadModel;
import com.movie.shop.movie.repository.MovieReadModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieQueryService {

    private final MovieReadModelRepository movieReadModelRepository;
    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 20);
    private static final String NOW_SHOWING = "NOW_SHOWING";

    @Transactional(readOnly = true)
    public List<MovieSummaryData> findNowShowingMovies() {
        return findNowShowingMovies("booking");
    }

    @Transactional(readOnly = true)
    public List<MovieSummaryData> findNowShowingMovies(String sortBy) {
        List<MovieReadModel> movies = switch (sortBy) {
            case "audience" -> movieReadModelRepository.findByStatusOrderByTotalAudienceCountDesc(NOW_SHOWING, DEFAULT_PAGE);
            case "rating" -> movieReadModelRepository.findByStatusOrderByAverageRatingDesc(NOW_SHOWING, DEFAULT_PAGE);
            case "latest" -> movieReadModelRepository.findByStatusOrderByReleaseDateDesc(NOW_SHOWING, DEFAULT_PAGE);
            default -> movieReadModelRepository.findByStatusOrderByBookingRateDesc(NOW_SHOWING, DEFAULT_PAGE);
        };
        
        return movies.stream()
                .map(MovieSummaryData::new)
                .collect(Collectors.toList());
    }
}