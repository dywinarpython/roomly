package com.project.roomly.dto.Booking;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingDto(@NotNull Long roomId, @NotNull LocalDate startTime, @NotNull LocalDate endTime ) {
}
