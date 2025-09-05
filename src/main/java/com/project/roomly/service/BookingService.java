package com.project.roomly.service;

import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;

public interface BookingService {

    ResponseBookingDto createBooking(BookingDto bookingDto, String uuid);



}
