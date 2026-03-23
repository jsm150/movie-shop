package com.movie.shop.api.screening.domain.policy.status;

public record MovieSchedulingAvailability(boolean schedulable, int runtimeMinutes) {
}
