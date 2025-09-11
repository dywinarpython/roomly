package com.project.roomly.repository;

import com.project.roomly.dto.Media.ResponseMediaDto;
import com.project.roomly.entity.RoomMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomMediaRepository extends JpaRepository<RoomMedia, Long> {

    @Query("select m.url from Room h join h.media m where h.id = :id")
    List<String> findMediaByRoomId(@Param("id") Long roomId);

    @Query(
            """
            select new com.project.roomly.dto.Media.ResponseMediaDto(r.id, m.url)
            from Room r
            join r.media m
            where r.id in :roomsId
            """
    )
    List<ResponseMediaDto> findMediasByRoomsId(@Param("roomsId") List<Long> roomsId);


    @Query("""
            select m.url
            from RoomMedia m
            where m.room is null
            """)
    List<String> findMediaForDelete();

    @Modifying
    @Query("delete from RoomMedia m WHERE m.room is null")
    int deleteByRoomIsNull();




    @Modifying
    @Query("""
            update RoomMedia r
            set r.room = null
            where r.url = :url and r.room is not null
            """)
    int updateMediaRoom(@Param("url") String key);

    @Query("select count(m) from RoomMedia m where m.room.id = :id")
    int countMediaByRoom(@Param("id") Long roomId);

}
