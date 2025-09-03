package com.project.roomly.repository;

import com.project.roomly.dto.Media.MediaDto;
import com.project.roomly.dto.Media.RoomsMediaDto;
import com.project.roomly.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("select new com.project.roomly.dto.Media.MediaDto(m.mediaType, m.url) from Hotel h join h.media m where h.id = :hotelId")
    List<MediaDto> findMediaByHotelId(@Param("hotelId") Long hotelId);

    @Query("select new com.project.roomly.dto.Media.MediaDto(m.mediaType, m.url) from Room h join h.media m where h.id = :roomId")
    List<MediaDto> findMediaByRoomId(@Param("roomId") Long roomId);

    @Query(
            """
            select new com.project.roomly.dto.Media.RoomsMediaDto(r.id, m.url, m.mediaType)
            from Room r
            join r.media m
            where r.id in :roomsId
            """
    )
    List<RoomsMediaDto> findMediasByRoomsId(@Param("roomsId") List<Long> roomsId);
}
