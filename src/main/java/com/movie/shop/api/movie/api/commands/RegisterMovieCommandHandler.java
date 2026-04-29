package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.port.MovieTitleUniquenessConditionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterMovieCommandHandler implements Command.Handler<RegisterMovieCommand, Long> {

    private final MovieRepository movieRepository;
    private final MovieTitleUniquenessConditionPort movieTitleUniquenessConditionPort;

    @Override
    @Transactional
    public Long handle(RegisterMovieCommand command) {
        var casts = command.casts().stream()
                .map(c -> new Actor(
                        c.name(),
                        c.dateOfBirth(),
                        c.national(),
                        c.role()
                ))
                .toList();

        var titleCondition = movieTitleUniquenessConditionPort.findCondition(command.title());

        var movie = Movie.register(
                titleCondition,
                command.title(),
                command.director(),
                command.genres(),
                command.runtimeMinutes(),
                command.audienceRating(),
                command.synopsis(),
                command.releaseDate(),
                casts
        );
        
        movieRepository.save(movie);
        
        return movie.getId();
    }
}
