package com.project.roomly.dto.Booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingDto(@NotNull Long roomId, @NotNull LocalDate startTime, @NotNull LocalDate endTime,
                         @NotNull @Min(value = 1, message = "The price cannot be less than or equal to 0.") BigDecimal priceDay,
                         @Min(0)    @Max(100)  Integer prepaymentPercentage) {

}
