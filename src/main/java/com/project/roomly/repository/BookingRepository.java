package com.project.roomly.repository;

import com.project.roomly.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(""" 
                select r.countRoom - count(b.id)
                from Room r
                left join Booking b
                  on b.room = r
                 and b.startTime < :endTime
                 and b.endTime > :startTime
                 and b.statusBooking <> com.project.roomly.entity.StatusBooking.CANCELLED
                where r.id = :roomId
                group by r.id, r.countRoom
                having count(b.id) < r.countRoom
            """)
    Optional<Integer> findNumberAvailableRooms(@Param("roomId") Long roomId,
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
            delete from Booking b
            where b.statusBooking = com.project.roomly.entity.StatusBooking.CANCELLED
            """)
    int deleteCancelledBooking();



}