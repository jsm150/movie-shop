package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "영화 수정 요청")
public record UpdateMovieCommand(
        @Schema(description = "영화 번호", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long movieId,
        
        @Schema(description = "영화 제목", example = "변경된 영화", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        
        @Schema(description = "감독", example = "변경된 감동", requiredMode = Schema.RequiredMode.REQUIRED)
        String director,
        
        @Schema(description = "장르 목록", example = "[\"로맨스\", \"공포\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> genres,
        
        @Schema(description = "러닝타임 (분)", example = "210", requiredMode = Schema.RequiredMode.REQUIRED)
        int runtimeMinutes,
        
        @Schema(description = "관람등급", example = "R18", requiredMode = Schema.RequiredMode.REQUIRED)
        AudienceRating audienceRating,
        
        @Schema(description = "시놉시스", example = "대충 변경된 내용", requiredMode = Schema.RequiredMode.REQUIRED)
        String synopsis,
        
        @Schema(description = "개봉일", example = "2025-12-20T09:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime releaseDate,
        
        @Schema(description = "출연 배우 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<UpdateActorDto> casts
) implements Command<Long> {
    
    @Schema(description = "배우 정보")
    public record UpdateActorDto(
            @Schema(description = "배우 이름", example = "변경된 배우", requiredMode = Schema.RequiredMode.REQUIRED)
            String name,
            
            @Schema(description = "생년월일", example = "2002-12-18T09:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
            OffsetDateTime dateOfBirth,
            
            @Schema(description = "국적", example = "대한민국", requiredMode = Schema.RequiredMode.REQUIRED)
            String national,
            
            @Schema(description = "배역", example = "주인공", requiredMode = Schema.RequiredMode.REQUIRED)
            String role
    ) {}
}
