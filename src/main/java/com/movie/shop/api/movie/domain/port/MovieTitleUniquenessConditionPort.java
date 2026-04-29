package com.movie.shop.api.movie.domain.port;

import com.movie.shop.api.movie.domain.condition.MovieTitleUniquenessCondition;

public interface MovieTitleUniquenessConditionPort {

    MovieTitleUniquenessCondition findCondition(String title);
}
