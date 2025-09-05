package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.dto.Room.RoomPaymentInfoDto;
import com.project.roomly.entity.Booking;
import com.project.roomly.entity.Room;
import com.project.roomly.mapper.MapperBooking;
import com.project.roomly.repository.BookingRepository;
import com.project.roomly.service.BookingService;
import com.project.roomly.service.RoomService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final MapperBooking mapperBooking;

    private final RoomService roomService;

    private final EntityManager entityManager;

    @Override
    @Transactional
    public ResponseBookingDto createBooking(BookingDto bookingDto, String uuid) {
        Room room = entityManager.find(Room.class, bookingDto.roomId(), LockModeType.PESSIMISTIC_WRITE);
        RoomPaymentInfoDto roomPaymentInfoDto = roomService.getRoomPaymentInfo(bookingDto.roomId());
        boolean priceChanged = roomPaymentInfoDto.priceDay().compareTo(bookingDto.priceDay()) != 0;
        boolean prepaymentChanged = roomPaymentInfoDto.prepaymentPercentage().compareTo(bookingDto.prepaymentPercentage()) != 0;

        if (priceChanged || prepaymentChanged) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Цена или процент предоплаты были изменены. (The prepayment price or percentage has been changed)"
            );
        }

        Optional<Integer> freeRoom = bookingRepository.findNumberAvailableRooms(bookingDto.roomId(), bookingDto.startTime(),  bookingDto.endTime());

        if(freeRoom.isEmpty() || freeRoom.get() == 0){
            throw new NoSuchElementException("No available rooms were found");
        }
        Booking booking = mapperBooking.bookingDtoToBooking(bookingDto, room, roomPaymentInfoDto.prepaymentPercentage(), uuid);
        bookingRepository.save(booking);
        return mapperBooking.bookingToResponseBookingDto(booking);
    }
}
