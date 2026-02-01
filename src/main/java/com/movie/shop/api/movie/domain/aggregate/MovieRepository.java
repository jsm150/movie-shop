package com.movie.shop.api.movie.domain.aggregate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    boolean existsByTitle(String title);
}
