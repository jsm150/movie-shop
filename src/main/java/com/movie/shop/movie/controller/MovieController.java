/**
 * MovieController.java
 * 
 * @description 영화 관련 HTTP 요청을 처리하는 컨트롤러 클래스.
 *              현재 상영 중인 영화 목록 조회 등의 기능을 제공한다.
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
 * @see com.movie.shop.movie.service.MovieQueryService
 * @see com.movie.shop.movie.service.MovieSummaryData
 */
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