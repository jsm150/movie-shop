package com.movie.shop.api.movie.api.commands;

import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.api.authorization.MovieManageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "영화 등록 요청")
public record RegisterMovieCommand(
        @Schema(description = "영화 제목", example = "인셉션", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        
        @Schema(description = "감독", example = "크리스토퍼 놀란", requiredMode = Schema.RequiredMode.REQUIRED)
        String director,
        
        @Schema(description = "장르 목록", example = "[\"액션\", \"SF\", \"스릴러\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> genres,
        
        @Schema(description = "러닝타임 (분)", example = "148", requiredMode = Schema.RequiredMode.REQUIRED)
        int runtimeMinutes,
        
        @Schema(description = "관람등급", example = "ALL", requiredMode = Schema.RequiredMode.REQUIRED)
        AudienceRating audienceRating,
        
        @Schema(description = "시놉시스", example = "꿈 속에서 생각을 훔치는 특수 요원의 이야기", requiredMode = Schema.RequiredMode.REQUIRED)
        String synopsis,
        
        @Schema(description = "개봉일", example = "2010-07-21T00:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime releaseDate,
        
        @Schema(description = "출연 배우 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ActorDto> casts
) implements MovieManageCommand<Long> {
    
    @Schema(description = "배우 정보")
    public record ActorDto(
            @Schema(description = "배우 이름", example = "레오나르도 디카프리오", requiredMode = Schema.RequiredMode.REQUIRED)
            String name,
            
            @Schema(description = "생년월일", example = "1974-11-11T00:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
            OffsetDateTime dateOfBirth,
            
            @Schema(description = "국적", example = "미국", requiredMode = Schema.RequiredMode.REQUIRED)
            String national,
            
            @Schema(description = "배역", example = "돔 코브", requiredMode = Schema.RequiredMode.REQUIRED)
            String role
    ) {}

}
