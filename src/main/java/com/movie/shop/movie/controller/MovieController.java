/**
 * MovieController.java
 * 
 * @description 영화 관련 HTTP 요청을 처리하는 컨트롤러 클래스.
 *              현재 상영 중인 영화 목록 조회 등의 기능을 제공한다.
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
 * @see com.movie.shop.movie.service.MovieQueryService
 * @see com.movie.shop.movie.service.MovieSummaryData
 */
package com.movie.shop.movie.controller;

import com.movie.shop.movie.service.MovieQueryService;
import com.movie.shop.movie.service.MovieSummaryData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieQueryService movieQueryService;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @GetMapping("/movies/now-showing")
    public String nowShowing(
            @RequestParam(value = "sort", defaultValue = "booking") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {
        
        // 페이지 사이즈 제한 (최대 100개)
        size = Math.min(size, 100);
        
        Page<MovieSummaryData> moviePage = movieQueryService.findNowShowingMovies(sort, page, size);

        if (moviePage.isEmpty()) {
            model.addAttribute("message", "현재 상영 중인 영화가 없습니다.");
        }

        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", moviePage.getNumber());
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("totalElements", moviePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", moviePage.hasPrevious());
        model.addAttribute("hasNext", moviePage.hasNext());

        return "movie/nowShowing"; // WEB-INF/views/movie/nowShowing.jsp
    }
}