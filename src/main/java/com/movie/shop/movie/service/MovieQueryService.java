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