package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    boolean existsByName_Name(String name);

    default Theater getById(long theaterId) {
        return this.findById(theaterId)
                .orElseThrow(() -> new TheaterDomainException("상영관 데이터가 존재하지 않습니다."));
    }
}
