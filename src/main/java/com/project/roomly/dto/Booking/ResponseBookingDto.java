package com.project.roomly.dto.Booking;

import java.time.LocalDate;
import java.math.BigDecimal;

public record ResponseBookingDto(
        Long bookingId,
        LocalDate startTime,
        LocalDate endTime,
        BigDecimal price,
        BigDecimal prepayment
) {}
