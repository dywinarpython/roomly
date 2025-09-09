package com.project.roomly.repository;

import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.entity.RoomMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomMediaRepository extends JpaRepository<RoomMedia, Long> {

    @Query("select m.url from Room h join h.media m where h.id = :roomId")
    List<String> findMediaByRoomId(@Param("roomId") Long roomId);

    @Query(
            """
            select new com.project.roomly.dto.Media.RoomsMediaDto(r.id, m.url)
            from Room r
            join r.media m
            where r.id in :roomsId
            """
    )
    List<RoomsMediaDto> findMediasByRoomsId(@Param("roomsId") List<Long> roomsId);

    @Query("""
            select m.url
            from Room r
            join r.media m
            where r.id in :roomsId
            """)
    List<String> findMediaKeysByRoomsId(@Param("roomsId") Long roomsId);
}
