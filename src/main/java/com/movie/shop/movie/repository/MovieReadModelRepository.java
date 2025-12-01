package com.movie.shop.movie.repository;

import com.movie.shop.movie.domain.MovieReadModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieReadModelRepository extends JpaRepository<MovieReadModel, Long> {
    // 예매율순 정렬
    List<MovieReadModel> findByStatusOrderByBookingRateDesc(String status, Pageable pageable);
    
    // 박스오피스순 (관객수) 정렬
    List<MovieReadModel> findByStatusOrderByTotalAudienceCountDesc(String status, Pageable pageable);
    
    // 평점순 정렬
    List<MovieReadModel> findByStatusOrderByAverageRatingDesc(String status, Pageable pageable);
    
    // 최신개봉작순 정렬
    List<MovieReadModel> findByStatusOrderByReleaseDateDesc(String status, Pageable pageable);
}