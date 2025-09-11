package com.project.roomly.dto.Booking;

import com.project.roomly.entity.StatusBooking;

import java.time.LocalDate;
import java.math.BigDecimal;

public record ResponseBookingDto(
        Long bookingId,
        Long roomId,
        LocalDate startTime,
        LocalDate endTime,
        BigDecimal price,
        BigDecimal prepayment,
        StatusBooking statusBooking
) {}