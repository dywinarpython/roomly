package com.project.roomly.repository;

import com.project.roomly.dto.Media.ResponseMediaDto;
import com.project.roomly.entity.HotelMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelMediaRepository extends JpaRepository<HotelMedia, Long> {

    @Query("select m.url from Hotel h join h.media m where h.id = :hotelId")
    List<String> findMediaKeyByHotelId(@Param("hotelId") Long hotelId);

    @Query("""
            select m.url
            from HotelMedia m
            where m.hotel is null
            """)
    List<String> findMediaForDelete();

    @Query(
            """
            select new com.project.roomly.dto.Media.ResponseMediaDto(h.id, m.url)
            from Hotel h
            join h.media m
            where h.id in :hotelsId
            """
    )
    List<ResponseMediaDto> findMediasByHotelsId(@Param("hotelsId") List<Long> hotelsId);

    @Modifying
    @Query("delete from HotelMedia m WHERE m.hotel is null")
    int deleteByHotelIsNull();

    @Modifying
    @Query("""
            update HotelMedia h
            set h.hotel = null
            where h.url = :url and h.hotel is not null
            """)
    int updateMediaHotel(@Param("url") String key);

    @Query("select count(m) from HotelMedia m where m.hotel.id = :hotelId")
    int countMediaByHotel(@Param("hotelId") Long hotelId);


}
