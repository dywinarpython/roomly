package com.project.roomly.repository;

import com.project.roomly.dto.Room.ResponseRoomDto;
import com.project.roomly.dto.Room.RoomPaymentInfoDto;
import com.project.roomly.dto.Room.SearchRoomDto;
import com.project.roomly.entity.Room;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Modifying
    @Query(value = """
        delete from room
        where id = :id
          and hotel_id in (select id from hotel where owner = :ownerId)
    """, nativeQuery = true)
    int deleteByIdAndHotelOwner(@Param("id") Long roomId,
                                @Param("ownerId") UUID owner);


    @Query("""
            select new com.project.roomly.dto.Room.ResponseRoomDto(r.id, r.name, r.description,  r.countRoom, r.priceDay, r.floor, r.hotel.prepaymentPercentage, r.hotel.id)
            from Room r
            where r.id = :id
            """)
    Optional<ResponseRoomDto> findRoom(@Param("id") Long roomId);

    @Query("""
            select new com.project.roomly.dto.Room.ResponseRoomDto(r.id, r.name, r.description,  r.countRoom, r.priceDay, r.floor,  r.hotel.prepaymentPercentage, r.hotel.id)
            from Room r
            where r.hotel.id = :hotelId
            """)
    List<ResponseRoomDto> findRoomsByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);




    @Query("""
       select case when (count(r) > 0) then true else false end
       from Room r
       where r.id = :id and r.hotel.owner = :owner
       """)
    boolean existsByRoomIdAndOwner(@Param("id") Long roomId, @Param("owner") UUID owner);



    @Query("""
            select new com.project.roomly.dto.Room.RoomPaymentInfoDto(r.priceDay, r.hotel.prepaymentPercentage)
            from Room r
            where r.id = :id
            """)
    Optional<RoomPaymentInfoDto> findRoomPricing(@Param("id") Long roomId);



    @Query("""
        select new com.project.roomly.dto.Room.SearchRoomDto(
               r.id,
               r.countRoom - count(b.id)
        )
        from Room r
        left join Booking b
          on b.room = r
         and b.startTime < :endTime
         and b.endTime   > :startTime
         and b.statusBooking <> com.project.roomly.entity.StatusBooking.CANCELLED
        where (:city is null or r.hotel.city = :city)
          and (:minPrice is null or r.priceDay >= :minPrice)
          and (:maxPrice is null or r.priceDay <= :maxPrice)
        group by r.id
        having r.countRoom - count(b.id) > 0
    """)
    List<SearchRoomDto> findAvailableRooms(@Param("city") String city,
                                           @Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice,
                                           @Param("startTime") LocalDate startTime,
                                           @Param("endTime") LocalDate endTime,
                                           Pageable pageable);




}
