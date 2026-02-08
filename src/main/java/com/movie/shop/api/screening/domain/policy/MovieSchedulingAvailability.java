package com.movie.shop.api.screening.domain.policy;

public record MovieSchedulingAvailability(boolean schedulable, int runtimeMinutes) {
}
