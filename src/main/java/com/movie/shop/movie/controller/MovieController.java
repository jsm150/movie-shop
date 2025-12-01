package com.movie.shop.movie.controller;

import com.movie.shop.movie.service.MovieQueryService;
import com.movie.shop.movie.service.MovieSummaryData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieQueryService movieQueryService;

    @GetMapping("/movies/now-showing")
    public String nowShowing(
            @RequestParam(value = "sort", defaultValue = "booking") String sort,
            Model model) {
        
        List<MovieSummaryData> movies = movieQueryService.findNowShowingMovies(sort);

        if (movies.isEmpty()) {
            model.addAttribute("message", "현재 상영 중인 영화가 없습니다.");
        }

        model.addAttribute("movies", movies);
        model.addAttribute("currentSort", sort);

        return "movie/nowShowing"; // WEB-INF/views/movie/nowShowing.jsp
    }
}