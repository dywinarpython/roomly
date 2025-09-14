package com.project.roomly.serviceimpl;

import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.dto.Booking.ResponseBookingsDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final MapperBooking mapperBooking;

    private final RoomService roomService;

    private final EntityManager entityManager;

    @Value("${pageable.size}")
    private Integer pageableSize;

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
        return mapperBooking.bookingToResponseBookingDto(booking, room.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseBookingDto getBooking(Long bookingId, String uuid) {
        Optional<ResponseBookingDto> optionalResponseBookingDto = bookingRepository.findBookingByIdAndUserId(bookingId, UUID.fromString(uuid));
        if(optionalResponseBookingDto.isEmpty()){
            if(bookingRepository.existsById(bookingId)){
                throw new AccessDeniedException("Access is denied");
            } else {
                throw new NoSuchElementException("Booking is not found!");
            }
        }
        return optionalResponseBookingDto.get();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseBookingsDto getBookings(String uuid, Integer page) {
        return new ResponseBookingsDto(bookingRepository.findBookingsById(UUID.fromString(uuid), PageRequest.of(page, pageableSize)));

    }

}
