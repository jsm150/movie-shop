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
public class ChangeStateMovieCommandHandler implements Command.Handler<ChangeStateMovieCommand, Voidy> {

    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Voidy handle(ChangeStateMovieCommand command) {
        Movie movie = movieRepository.getById(command.movieId());

        switch (command.status()) {
            case COMING_SOON -> movie.moveToComingSoon();
            case NOW_SHOWING -> movie.startShowing();
            case ENDED -> movie.endShowing();
        }

        return null;
    }
}
