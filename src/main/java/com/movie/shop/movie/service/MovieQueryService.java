/**
 * MovieQueryService.java
 * 
 * @description 영화 조회 관련 비즈니스 로직을 처리하는 서비스 클래스.
 *              현재 상영 중인 영화 목록을 다양한 정렬 기준으로 조회하는 기능을 제공한다.
 * 
 * @author      movie-shop Development Team
 * @version     1.0.1
 * @since       2025-12-02
 * 
 * @modification
 * <pre>
 * 수정일        수정자       수정내용
 * ----------  --------    ---------------------------
 * 2025-12-02   정수민       최초 생성
 * 2025-12-05   정수민       페이징 기능 적용 (1.0.1)
 * </pre>
 * 
 * @see com.movie.shop.movie.repository.MovieReadModelRepository
 * @see com.movie.shop.movie.service.MovieSummaryData
 */
package com.movie.shop.movie.service;

import com.movie.shop.movie.domain.MovieReadModel;
import com.movie.shop.movie.repository.MovieReadModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieQueryService {

    private final MovieReadModelRepository movieReadModelRepository;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String NOW_SHOWING = "NOW_SHOWING";

    @Transactional(readOnly = true)
    public Page<MovieSummaryData> findNowShowingMovies() {
        return findNowShowingMovies("booking", 0, DEFAULT_PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public Page<MovieSummaryData> findNowShowingMovies(String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        Page<MovieReadModel> movies = switch (sortBy) {
            case "audience" -> movieReadModelRepository.findByStatusOrderByTotalAudienceCountDesc(NOW_SHOWING, pageable);
            case "rating" -> movieReadModelRepository.findByStatusOrderByAverageRatingDesc(NOW_SHOWING, pageable);
            case "latest" -> movieReadModelRepository.findByStatusOrderByReleaseDateDesc(NOW_SHOWING, pageable);
            default -> movieReadModelRepository.findByStatusOrderByBookingRateDesc(NOW_SHOWING, pageable);
        };
        
        return movies.map(MovieSummaryData::new);
    }
}