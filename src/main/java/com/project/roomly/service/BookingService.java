package com.project.roomly.service;

import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.dto.Booking.ResponseBookingsDto;

public interface BookingService {

    ResponseBookingDto createBooking(BookingDto bookingDto, String uuid);

    ResponseBookingDto getBooking(Long bookingId, String uuid);

    ResponseBookingsDto getBookings(String uuid, Integer page);


}
