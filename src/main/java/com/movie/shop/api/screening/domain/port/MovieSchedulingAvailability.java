package com.movie.shop.api.screening.domain.port;

public record MovieSchedulingAvailability(boolean schedulable, int runtimeMinutes) {
}
