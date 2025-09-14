package com.project.roomly.mapper;

import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.entity.Booking;
import com.project.roomly.entity.Room;
import com.project.roomly.entity.StatusBooking;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperBooking {

    default Booking bookingDtoToBooking(BookingDto bookingDto, Room room, Integer prepaymentPercentage, String uuid){
        if(bookingDto == null){
            return null;
        }
        Booking booking = new Booking();
        booking.setStatusBooking(StatusBooking.AWAIT_PAY);
        booking.setRoom(room);
        booking.setPrice(
                bookingDto.priceDay().multiply(BigDecimal.valueOf(
                        ChronoUnit.DAYS.between(bookingDto.startTime(), bookingDto.endTime())))
        );
        booking.setPrepayment(booking.getPrice().multiply(BigDecimal.valueOf(prepaymentPercentage)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        booking.setEndTime(bookingDto.endTime());
        booking.setStartTime(bookingDto.startTime());
        booking.setUserId(UUID.fromString(uuid));
        booking.setCreateTime(LocalDateTime.now());
        return booking;
    }

    @Mappings(
            value = {
                    @Mapping(source = "id", target = "bookingId"),
                    @Mapping(target = "roomId", expression = "java(roomId)")
            })
    ResponseBookingDto bookingToResponseBookingDto(Booking booking, @Context Long roomId);
}
