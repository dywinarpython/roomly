package com.project.roomly.repository;

import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.entity.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(""" 
                select r.countRoom - count(b.id)
                from Room r
                left join Booking b
                  on b.room = r
                 and b.startTime < :endTime
                 and b.endTime > :startTime
                 and b.statusBooking <> com.project.roomly.entity.StatusBooking.CANCELLED
                where r.id = :id
                group by r.id, r.countRoom
                having count(b.id) < r.countRoom
            """)
    Optional<Integer> findNumberAvailableRooms(@Param("id") Long roomId,
                                               @Param("startTime") LocalDate startTime,
                                               @Param("endTime") LocalDate endTime);


    @Modifying
    @Query("""
            update Booking b
            set b.statusBooking = com.project.roomly.entity.StatusBooking.CANCELLED
            where b.createTime < :currentTime and b.statusBooking != com.project.roomly.entity.StatusBooking.CANCELLED
            """)
    int cancelExpiredBookings(@Param("currentTime")LocalDateTime currentTime);

    @Modifying
    @Query("""
            update Booking b
            set b.statusBooking = com.project.roomly.entity.StatusBooking.CANCELLED
            where b.id = :bookingId and b.statusBooking != com.project.roomly.entity.StatusBooking.CANCELLED
            """)
    int cancelExpiredBookings(@Param("bookingId") Long bookingId);


    @Modifying
    @Query("""
            delete from Booking b
            where b.statusBooking = com.project.roomly.entity.StatusBooking.CANCELLED
            """)
    int deleteCancelledBooking();


    @Query("""
            select new com.project.roomly.dto.Booking.ResponseBookingDto(b.id, b.room.id, b.startTime, b.endTime, b.price, b.prepayment, b.statusBooking)
            from Booking b
            where b.id = :bookingId and b.userId = :owner
            """)
    Optional<ResponseBookingDto> findBookingByIdAndUserId(@Param("bookingId") Long bookingId, @Param("owner") UUID owner);


    @Query("""
            select new com.project.roomly.dto.Booking.ResponseBookingDto(b.id, b.room.id, b.startTime, b.endTime, b.price, b.prepayment, b.statusBooking)
            from Booking b
            where b.userId = :owner
            """)
    List<ResponseBookingDto> findBookingsById(@Param("owner") UUID owner, Pageable pageable);





}