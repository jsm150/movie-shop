package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.port.MovieScreeningPresencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteMovieCommandHandler implements Command.Handler<DeleteMovieCommand, Voidy> {

    private final MovieRepository movieRepository;
    private final MovieScreeningPresencePort movieScreeningPresencePort;

    @Transactional
    @Override
    public Voidy handle(DeleteMovieCommand command) {
        Movie movie = movieRepository.getById(command.movieId());
        var screeningPresence = movieScreeningPresencePort.findPresence(movie.getId());

        movieRepository.delete(movie, screeningPresence);
        return null;
    }
}
