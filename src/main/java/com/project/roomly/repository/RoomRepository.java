package com.project.roomly.repository;

import com.project.roomly.dto.Room.ResponseRoomDto;
import com.project.roomly.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            select new com.project.roomly.dto.Room.ResponseRoomDto(r.name, r.countRoom, r.priceDay)
            from Room r
            where r.id = :roomId
            """)
    Optional<ResponseRoomDto> findRoom(@Param("roomId") Long roomId);
}
