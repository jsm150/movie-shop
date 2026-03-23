package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.policy.MovieTitlePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateMovieCommandHandler implements Command.Handler<UpdateMovieCommand, Long> {

    private final MovieRepository movieRepository;
    private final MovieJpaPort movieJpaPort;

    @Override
    @Transactional
    public Long handle(UpdateMovieCommand command) {
        Movie movie = movieRepository.getById(command.movieId());
        
        var casts = command.casts().stream()
                .map(c -> new Actor(
                        c.name(),
                        c.dateOfBirth(),
                        c.national(),
                        c.role()
                ))
                .toList();

        MovieTitlePolicy movieTitleDuplicateValidator =
                new MovieTitlePolicy(movieJpaPort.loadTitleDuplication(command.title()));

        movie.Update(
                movieTitleDuplicateValidator,
                command.title(),
                command.director(),
                command.genres(),
                command.runtimeMinutes(),
                command.audienceRating(),
                command.synopsis(),
                command.releaseDate(),
                casts
        );
        
        return command.movieId();
    }
}
