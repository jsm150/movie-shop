package com.movie.shop.api.screening.domain.condition;

public record MovieSchedulingCondition(boolean canBeScheduled, int runtimeMinutes) {
}
