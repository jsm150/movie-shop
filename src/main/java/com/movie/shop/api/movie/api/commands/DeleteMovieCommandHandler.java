package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteMovieCommandHandler implements Command.Handler<DeleteMovieCommand, Voidy> {

    private final MovieRepository movieRepository;

    @Transactional
    @Override
    public Voidy handle(DeleteMovieCommand command) {
        Movie movie = movieRepository.getById(command.movieId());
        movieRepository.delete(movie);
        return null;
    }
}
