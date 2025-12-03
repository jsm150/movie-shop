/**
 * MovieReadModelRepository.java
 * 
 * @description 영화 읽기 모델에 대한 데이터 접근 계층 인터페이스.
 *              JpaRepository를 확장하여 다양한 정렬 기준의 영화 목록 조회 기능을 제공한다.
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
 * @see com.movie.shop.movie.domain.MovieReadModel
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
package com.movie.shop.movie.repository;

import com.movie.shop.movie.domain.MovieReadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieReadModelRepository extends JpaRepository<MovieReadModel, Long> {
    // 예매율순 정렬
    Page<MovieReadModel> findByStatusOrderByBookingRateDesc(String status, Pageable pageable);
    
    // 박스오피스순 (관객수) 정렬
    Page<MovieReadModel> findByStatusOrderByTotalAudienceCountDesc(String status, Pageable pageable);
    
    // 평점순 정렬
    Page<MovieReadModel> findByStatusOrderByAverageRatingDesc(String status, Pageable pageable);
    
    // 최신개봉작순 정렬
    Page<MovieReadModel> findByStatusOrderByReleaseDateDesc(String status, Pageable pageable);
}