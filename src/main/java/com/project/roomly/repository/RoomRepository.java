package com.project.roomly.repository;

import com.project.roomly.dto.Room.ResponseRoomDto;
import com.project.roomly.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Modifying
    @Query(value = """
        delete from room
        where id = :roomId
          and hotel_id in (select id from hotel where owner = :ownerId)
    """, nativeQuery = true)
    int deleteByIdAndHotelOwner(@Param("roomId") Long roomId,
                                @Param("ownerId") UUID owner);

    boolean existsById(Long id);

    @Query("""
            select new com.project.roomly.dto.Room.ResponseRoomDto(r.id, r.name, r.countRoom, r.priceDay, r.floor)
            from Room r
            where r.id = :roomId
            """)
    Optional<ResponseRoomDto> findRoom(@Param("roomId") Long roomId);

    @Query("""
            select new com.project.roomly.dto.Room.ResponseRoomDto(r.id, r.name, r.countRoom, r.priceDay, r.floor)
            from Room r
            where r.hotel.id = :hotelId
            """)
    List<ResponseRoomDto> findRoomsByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
       select case when (count(r) > 0) then true else false end
       from Room r
       where r.id = :roomId and r.hotel.owner = :owner
       """)
    boolean existsByRoomIdAndOwner(@Param("roomId") Long roomId, @Param("owner") UUID owner);


}
